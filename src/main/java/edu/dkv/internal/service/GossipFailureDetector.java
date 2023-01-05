package edu.dkv.internal.service;

import edu.dkv.internal.entities.Member;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GossipFailureDetector implements FailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final GossipMessageService msgService;
    private final UserProcess memberProcess;
    private final UserProcess introducer;
    private final Member member;

    public GossipFailureDetector(UserProcess memberProcess, UserProcess introducer) {
        this.msgService = new GossipMessageService(memberProcess);
        this.memberProcess = memberProcess;
        this.introducer = introducer;
        this.member = new Member(memberProcess.getEndPoint());
    }

    /**
     * A member process is introduced to group of members that run another
     * instance of the Failure Detector.
     */
    @Override
    public void introduceSelfToGroup() {
        logger.info("Introducing self to group");
    }

    /**
     * Sends heartbeats using any failure detector protocol.
     */
    @Override
    public void sendHeartbeats() {
        logger.info("Sending heartbeats");
    }

    /**
     * Receives heartbeats using any failure detector protocol.
     */
    @Override
    public void receiveHeartbeats() {
        logger.info("Receive heartbeats");
    }

    /**
     * Method to mark the current member as failed and terminate the process
     * i.e. exit the failure-detector-service.
     *
     * @return - service status.
     */
    @Override
    public boolean exit() {
        return false;
    }
}
