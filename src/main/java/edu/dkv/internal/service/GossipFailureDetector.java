package edu.dkv.internal.service;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.entities.*;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

public class GossipFailureDetector implements FailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final long TFAIL;
    private final long TREMOVE;
    private final long TGOSSIP;

    private final GossipMessageService msgService;
    private final UserProcess memberProcess;
    private final UserProcess introducer;
    private final Member member;

    public GossipFailureDetector(UserProcess memberProcess, UserProcess introducer, AppConfig appConfig) {
        this.msgService = new GossipMessageService(memberProcess);
        this.memberProcess = memberProcess;
        this.introducer = introducer;
        this.member = new Member();

        TFAIL = appConfig.gossipConfig().getTFail();
        TREMOVE = appConfig.gossipConfig().getTRemove();
        TGOSSIP = appConfig.gossipConfig().getTGossip();
    }

    /**
     * Initializes the member node
     * Sets the inited flag to true and resets the counters.
     * Also, initializes the membership list with the current member node entry.
     *
     * @return - true if init succeeded.
     */
    @Override
    public boolean initNode() {
        logger.debug("Initializing the member node");
        try {
            member.setInited(true);

            // Setting the current node details
            member.setNeighborCount(0);
            member.setHeartbeat(0);
            member.setPingCounter(TFAIL);
            member.setTimeOutCounter(-1);

            // Initializing the member ship list entry with the current member node.
            MemberListEntry nodeEntry = new MemberListEntry(memberProcess.getEndPoint(), 0, System.currentTimeMillis());
            member.addMemberListEntry(nodeEntry);
            logger.trace("Node-entry: {}", nodeEntry);

        } catch (Exception e){
            logger.error("Exception encounterd while initializing member node.\n{}", Utils.getFullStackTrace(e));
            return false;
        }
        logger.info("Member Node initialized");
        return true;
    }

    /**
     * A member process is introduced to group of members that run another
     * instance of the Failure Detector.
     *
     * @return - true if introduction succeeded.
     */
    @Override
    public boolean introduceSelfToGroup() {
        try {
            if (isIntroducer()) {
                logger.info("Member node is the introducer");

                // The current member node is the introducer, therefore no JOINREQ messages need to be
                // sent and the member node can introduce itself to the group directly.
                member.setInGroup(true);

            } else {
                logger.info("Introducing self to group");

                // Constructing the JOINREQ message to be sent to the introducer for joining the group.
                MembershipMessage msg = MembershipMessage.createMessage()
                        .setMessageType(MessageType.JOINREQ)
                        .setEndPoint(member.getEndPoint())
                        .setHeartbeat(member.getHeartbeat())
                        .build();
                logger.trace("Msg: {}", msg);

                // Sending JOINREQ message to the introducer via Message Service
                // that uses the underlying NetworkService.
                msgService.sendMessage(introducer.getEndPoint(), msg);
            }
        } catch (Exception e){
            logger.error("Exception encountered while introducing self to the group: \n{}", Utils.getFullStackTrace(e));
            return false;
        }
        return true;
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

    public boolean isIntroducer(){
        return memberProcess.equals(introducer);
    }
}
