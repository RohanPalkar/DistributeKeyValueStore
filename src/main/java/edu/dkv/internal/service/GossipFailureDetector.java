package edu.dkv.internal.service;

import edu.dkv.app.ProcessBasedApplication;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class GossipFailureDetector implements FailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final GossipMessageService msgService;
    private final UserProcess introducer;

    public GossipFailureDetector(GossipMessageService msgService, UserProcess introducer) {
        this.msgService = msgService;
        this.introducer = introducer;
    }

    @Override
    public void introduceSelfToGroup() {
        System.out.println("Introducing self to the group");
    }

    @Override
    public void sendHeartbeats() {

    }

    public void detectFailures() {

    }

    @Override
    public void receiveHeartbeats() {

    }
}
