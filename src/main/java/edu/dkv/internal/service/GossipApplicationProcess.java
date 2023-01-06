package edu.dkv.internal.service;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.dkv.internal.common.Utils.getFullStackTrace;

public class GossipApplicationProcess implements Callable<Boolean> {

    private final static Logger logger = LogManager.getLogger(GossipApplicationProcess.class);

    private final FailureDetector failureDetector;
    private final UserProcess memberProcess;
    private final AtomicBoolean cancel;

    public GossipApplicationProcess(int index, UserProcess introducerProcess, AppConfig appConfig) {

        // Checks if the current member process is the same as introducer. If yes, then uses the
        // introducer instance itself to assign to member process.
        this.memberProcess = introducerProcess.getProcessId() == index ?
                introducerProcess : new UserProcess(index, Utils.getPort(appConfig.processConfig()));
        cancel = new AtomicBoolean(false);

        // Implementation of Gossip Style Failure Detector.
        this.failureDetector =
                new GossipFailureDetectorService(memberProcess, introducerProcess, appConfig, cancel);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() {
        boolean status = false;
        ThreadContext.push(memberProcess.getProcessName());
        try {
            status = failureDetector.runFailureDetector();
            logger.info("Failure Detector Status for member {} : {}", memberProcess.getProcessName(), status);
        } catch (Exception e) {
            logger.error("ERROR: Exception occurred while running failure detector.\n{}", getFullStackTrace(e));
        } finally {
            ThreadContext.pop();
        }
        return status;
    }

    /**
     * Shuts down the Application Process by setting the 'isRunning' instance to false.
     */
    public void shutdown(){
        logger.debug("Shutting-down the GossipApplicationProcess: {}", memberProcess.getProcessName());
        cancel.set(true);
    }
}
