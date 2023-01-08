package edu.dkv.internal.service;

import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.dkv.internal.common.Utils.*;

public class GossipFailureDetectorService implements FailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final UserProcess memberProcess;
    private final AtomicBoolean doCancel;
    private final GossipFailureDetector failureDetector;
    private final long TGOSSIP;

    public GossipFailureDetectorService(UserProcess memberProcess, UserProcess introducer, AppConfig appConfig, AtomicBoolean doCancel) {
        this.memberProcess = memberProcess;
        this.doCancel = doCancel;
        this.failureDetector = new GossipFailureDetector(memberProcess, introducer, appConfig.gossipConfig());
        TGOSSIP = appConfig.gossipConfig().getTGossip();
    }

    /**
     * Primary implementation method for running the failure detector.
     * @return - status of the operation
     */
    @Override
    public boolean runFailureDetector() {
        boolean status = false;
        logger.info("Initializing the failure-detector service");

        // Initializing the node & it's membership list with the member process
        if(!failureDetector.initNode())
            return status;

        // Joining the group and sending JOINREQ messages to the introducer.
        if(!failureDetector.introduceSelfToGroup())
            return status;

        // Once the node is initialized and introduced the group, there are 3 parallel
        // tasks created for the purpose of
        // 1. Heartbeat Processing - a task scheduled to execute periodically at a fixed rate
        // until cancelled/shutdown.
        // 2. InChannel - a task that runs continuously listening to any messages from the
        // network-service.
        // 3. Cancel - a task that runs in parallel watching for the 'doCancel' flag status
        // and if it's true, issues a shutdown call to the above 2 tasks.

        ScheduledExecutorService gossipScheduler = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Both the task instances.
        HeartbeatProcess heartbeat = new HeartbeatProcess(failureDetector, memberProcess);
        InChannel channel = new InChannel(failureDetector, memberProcess);
        logger.info("Running the failure-detector service");

        // Future # 1 - Heartbeat Processor Future
        CompletableFuture<Void> heartbeatFuture = CompletableFuture.supplyAsync(() -> {
            ThreadContext.push(memberProcess.getProcessName());
            try {
                logger.debug("Scheduling heartbeat processer at fixed rate of {} seconds", TGOSSIP);
                ScheduledFuture<?> future = gossipScheduler.scheduleAtFixedRate(heartbeat, 0, TGOSSIP, TimeUnit.SECONDS);
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Exception encountered with the heartbeat process scheduler.\n{}", getFullStackTrace(e));
            } finally {
                ThreadContext.pop();
            }
            return null;
        });

        // Future # 2 - Incoming Message Channel Future
        CompletableFuture<Void> msgChannelFuture = CompletableFuture.supplyAsync(() -> {
            ThreadContext.push(memberProcess.getProcessName());
            try {
                logger.debug("Preparing the Msg-Channel task");
                Future<?> future = executor.submit(channel);
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Exception encountered with the msg-channel-future.\n{}", getFullStackTrace(e));
            } finally {
                ThreadContext.pop();
            }
            return null;
        });

        // Future # 3 - Cancellation Future
        logger.debug("Preparing the Cancelling-future task");
        CompletableFuture<Void> cancelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Future<?> future = executor.submit(() -> {
                    ThreadContext.push(memberProcess.getProcessName());
                    try {
                        // Do Nothing. Keep executing and waiting for a cancel signal from the higher application layer.
                        while (!doCancel.get()) {}

                        // Upon receiving a cancel from app layer, individual tasks of heartbeat and channel
                        // will be terminated by marking the inner atomic-flag for shutdown.
                        heartbeat.shutdown();
                        channel.shutdown();

                        // Although the tasks are set to shutdown, the future instances are still running.
                        // They also need to be cancelled.
                        heartbeatFuture.cancel(true);
                        msgChannelFuture.cancel(true);
                    } catch (Exception e){
                        logger.error("Exception encountered with the cancel-task\n{}", getFullStackTrace(e));
                    } finally {
                        ThreadContext.pop();
                    }
                });
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Exception encountered with the cancel future.\n{}", getFullStackTrace(e));
            }
            return null;
        });

        // Combining all futures together and executing them.
        try {
            CompletableFuture
                    .allOf(heartbeatFuture, msgChannelFuture, cancelFuture)
                    .join();
        } catch (CompletionException e){
            logger.warn("Tasks completed upon cancellation. {}", e.getMessage());
        }

        logger.debug("Checking for active services");
        status = heartbeatFuture.isDone() && msgChannelFuture.isDone() && cancelFuture.isDone();
        logger.info("Failure detector task statuses : {}", status);
        if(!status)
            logger.error("All failure-detector tasks are not terminated");


        shutDownExecutor(executor);
        shutDownScheduler(gossipScheduler);
        logger.info("Shutting-down the failure-detector service");
        return true;
    }

    public static class HeartbeatProcess implements Runnable {

        private final UserProcess memberProcess;
        private final GossipFailureDetector failureDetector;
        private final AtomicBoolean isRunning;

        public HeartbeatProcess(GossipFailureDetector failureDetector, UserProcess memberProcess) {
            this.failureDetector = failureDetector;
            this.memberProcess = memberProcess;
            isRunning = new AtomicBoolean(true);
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            ThreadContext.push(memberProcess.getProcessName());
            try {
                if (isRunning.get())
                    failureDetector.processHeartbeats();
            } catch (Exception e){
                logger.error("Exception encontered with Heartbeat Process Task.\n{}", getFullStackTrace(e));
            } finally {
                ThreadContext.pop();
            }
        }

        /**
         * Shuts down the member node and all it's concerned tasks.
         */
        public void shutdown(){
            logger.debug("Shutting-down Heartbeat Processor");
            isRunning.set(false);
            failureDetector.exit();
        }
    }

    public static class InChannel implements Callable<Void> {

        private final UserProcess memberProcess;
        private final GossipFailureDetector failureDetector;
        private final AtomicBoolean isRunning;

        public InChannel(GossipFailureDetector failureDetector, UserProcess memberProcess) {
            this.memberProcess = memberProcess;
            this.failureDetector = failureDetector;
            isRunning = new AtomicBoolean(true);
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Void call() {
            ThreadContext.push(memberProcess.getProcessName());
            try {
                while (isRunning.get()) {
                    failureDetector.msgInChannel();
                }
            } catch (Exception e){
                logger.error("Exception encountered with InChannel task.\n{}", getFullStackTrace(e));
            } finally {
                ThreadContext.pop();
            }
            return null;
        }

        public void shutdown(){
            logger.debug("Shutting-down the MsgChannel");
            isRunning.set(false);
        }
    }
}
