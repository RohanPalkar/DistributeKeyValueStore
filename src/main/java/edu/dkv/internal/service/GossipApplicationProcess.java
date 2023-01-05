package edu.dkv.internal.service;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.ProcessConfig;
import edu.dkv.internal.entities.UserProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class GossipApplicationProcess implements Callable<Boolean> {

    private final static Logger logger = LogManager.getLogger(GossipApplicationProcess.class);

    private final GossipFailureDetectorService gossipFailureDetectorService;
    private final UserProcess memberProcess;
    private final AtomicBoolean cancel;

    public GossipApplicationProcess(int index, UserProcess introducerProcess, ProcessConfig processConfig) {
        this.memberProcess = introducerProcess.getProcessId() == index ?
                introducerProcess : new UserProcess(index, Utils.getPort(processConfig));
        logger.debug("Member Process: {}", memberProcess);
        cancel = new AtomicBoolean(false);
        this.gossipFailureDetectorService = new GossipFailureDetectorService(memberProcess, introducerProcess, cancel);
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
            gossipFailureDetectorService.run();
            status = true;
        } catch (Exception e) {
            logger.error("ERROR: Exception occured while running failure detector.\n{}",
                    Utils.getFullStackTrace(e));
        } finally {
            ThreadContext.pop();
        }
        return status;
    }

    /**
     * Shutdowns the Application Process by setting the 'isRunning' instance to false.
     */
    public void shutdown(){
        logger.debug("Shutting-down the GossipApplicationProcess: {}", memberProcess.getProcessName());
        cancel.set(true);
    }
}
