package edu.dkv.app;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.config.ProcessConfig;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.internal.service.GossipApplicationProcess;
import edu.dkv.sdk.KVStoreApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.dkv.internal.common.Utils.shutDownExecutor;
import static edu.dkv.internal.common.Utils.shutDownScheduler;

@Component
public class GossipApplication implements KVStoreApplication {

    private final static Logger logger = LogManager.getLogger(GossipApplication.class);

    private final AppConfig appConfig;
    private final ProcessConfig processConfig;
    private final long maxTime;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler; //ScheduledThreadPoolExecutor
    private final int processCount;
    private final int maxThreads;

    @Autowired
    public GossipApplication(AppConfig appConfig) {
        this.appConfig = appConfig;
        logger.trace("Application-Configuration: {}", appConfig);
        this.processConfig = appConfig.processConfig();

        this.maxTime = appConfig.getRunningTime();
        logger.debug("MaxTime: {}", maxTime);

        this.processCount = processConfig.getCount();
        this.maxThreads = processConfig.getMaxThreads();
        logger.info("Process Count: {}, Max-Threads: {}", processCount, maxThreads);

        // The primary executor that activates each process on each thread.
        executorService = Executors.newFixedThreadPool(processCount);

        // The scheduler that terminates each process at the end of the
        // given time interval. RemoteOnCancelPolicy is set to true to
        // remove the process from the scheduler queue immediately.
        scheduler = Executors.newScheduledThreadPool(processCount); //new ScheduledThreadPoolExecutor(processCount);
        //scheduler.setRemoveOnCancelPolicy(true);
    }

    /**
     * Runs the Distributed Key-Value Store Application.
     *
     * Simulates the running of the application until the maximum running time specified
     * in the properties file.
     */
    @Override
    public void run() {
        logger.info("Running GossipApplication...");

        // Finding the index of the process that will act as the introducer.
        // TODO: Move to consensus based approach later on.
        int introducerId = Utils.getRandomNumber(0, processCount);
        UserProcess introducerProcess = new UserProcess(introducerId, Utils.getPort(processConfig));

        List<CompletableFuture<Boolean>> processesFuture = IntStream
                .range(0, processCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    logger.debug("Initiating the GossipApplicationProcess #{}", i);

                    // Primary future for creating the application process which is submitted by the executor service.
                    GossipApplicationProcess gap = new GossipApplicationProcess(i, introducerProcess, appConfig);
                    Future<Boolean> gapFuture = executorService.submit(gap);

                    // Scheduler for submitting a cancellation or shutdown call to the above futures
                    // after the prescribed max-time. The future.get(timeout) does not really terminate
                    // the process or interupt it. Hence, a parallel scheduler is used to issue a
                    // cancel/terminate call to each GossipApplicationProcess.
                    scheduler.schedule(gap::shutdown, maxTime, TimeUnit.MILLISECONDS);

                    // Getting the gossip counts from the future.
                    boolean status = false;
                    try {
                        status = gapFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("ERROR: Exception encountered during future get.\n{}", Utils.getFullStackTrace(e));
                    }
                    return status;
                }))
                .collect(Collectors.toList());

        // Combining all the futures together - so that they get executed in parallel together.
        // Join will help combine their results into one.
        CompletableFuture
                .allOf(processesFuture.toArray(new CompletableFuture<?>[processesFuture.size()]))
                .join();

        // Making sure that all processes are terminated
        AtomicInteger activeProcess = new AtomicInteger(processCount);
        logger.debug("Active Processes: {}", activeProcess.get());
        while(activeProcess.get() > 0){
            processesFuture.forEach(p -> {
                if(p.isDone() || p.isCompletedExceptionally())
                    activeProcess.decrementAndGet();
            });
            logger.debug("Active Processes: {}", activeProcess.get());
        }

        // Cleanup Activities. Shutting-down all executors and threads.
        shutDownExecutor(executorService);
        shutDownScheduler(scheduler);
        logger.info("Application shutdown");
    }
}
