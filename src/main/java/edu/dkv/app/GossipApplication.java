package edu.dkv.app;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.config.ProcessConfig;
import edu.dkv.internal.service.GossipApplicationProcess;
import edu.dkv.sdk.KVStoreApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GossipApplication implements KVStoreApplication {

    private final static Logger logger = LogManager.getLogger(GossipApplication.class);

    private final ProcessConfig processConfig;
    private final long maxTime;
    private final ExecutorService executorService;
    private final int processCount;
    private final int[] portsList;

    @Autowired
    public GossipApplication(AppConfig appConfig) {
        this.processConfig = appConfig.processConfig();
        this.maxTime = appConfig.getRunningTime();
        this.processCount = processConfig.getCount();
        logger.info("Process Count: {}", processCount);
        this.executorService = Executors.newFixedThreadPool(processCount);
        this.portsList = new int[processCount];

        // Populating the port list with the ports sequentially, starting from the PORT_RANGE_START
        IntStream.range(0, processCount)
                .forEach(i -> portsList[i] = processConfig.getPortRangeStart() + i);
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
        // This will be moved to consensus based approach later on.
        int introducerId = Utils.getRandomNumber(0, processCount);

        List<CompletableFuture<Boolean>> processesFuture = IntStream
                .range(0, processCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(new GossipApplicationProcess(i, introducerId), executorService))
                .collect(Collectors.toList());

        boolean result = processesFuture.stream()
                .map(CompletableFuture::join)
                .reduce(true, (a,b) -> a & b);
        logger.info("Application ended as {}", result);

        Assert.isTrue(result, "Overall Result is FALSE");
    }
}
