package edu.dkv.internal.service;

import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class GossipFailureDetectorService implements Runnable {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final AtomicBoolean doCancel;
    private final FailureDetector failureDetector;

    public GossipFailureDetectorService(UserProcess memberProcess, UserProcess introducer, AtomicBoolean doCancel) {
        this.doCancel = doCancel;
        this.failureDetector = new GossipFailureDetector(memberProcess, introducer);
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
        logger.debug("Running the failure-detector service");
        failureDetector.introduceSelfToGroup();
        while(!doCancel.get() && !failureDetector.exit()){
            failureDetector.sendHeartbeats();
            failureDetector.receiveHeartbeats();
        }
    }
}
