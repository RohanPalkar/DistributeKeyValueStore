package edu.dkv.internal.service;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.config.GossipConfig;
import edu.dkv.internal.entities.*;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

import static edu.dkv.internal.common.Utils.getFullStackTrace;

public class GossipFailureDetector extends AbstractFailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final long TFAIL;
    private final long TREMOVE;


    private final GossipMessageService msgService;
    private final UserProcess memberProcess;
    private final UserProcess introducer;
    private final Member member;

    public GossipFailureDetector(UserProcess memberProcess, UserProcess introducer, GossipConfig gossipConfig) {
        this.msgService = new GossipMessageService(memberProcess);
        this.memberProcess = memberProcess;
        this.introducer = introducer;
        this.member = new Member();

        TFAIL = gossipConfig.getTFail();
        TREMOVE = gossipConfig.getTRemove();
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
            logger.error("Exception encounterd while initializing member node.\n{}", getFullStackTrace(e));
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
            logger.error("Exception encountered while introducing self to the group: \n{}", getFullStackTrace(e));
            return false;
        }
        return true;
    }

    /**
     * Primary method for performing the membership protocol duties.
     *
     * @return - true if all operations succeeded
     */
    @Override
    public boolean processHeartbeats() {
        try {
            logger.info("Processing Hearbeats");

            if(!member.isFailed()) {
                logger.debug("Checking messages/heartbeats");
                checkMessages();
            } else {
                logger.error("Member node {} has failed", memberProcess.getProcessName());
                return false;
            }

            if(member.isInGroup() && !member.isFailed()) {
                logger.debug("Sending heartbeats");
                sendHeartbeats();
            } else {
                logger.error("Member node {} has either failed or is not in the group", memberProcess.getProcessName());
                return false;
            }

        } catch (Exception e){
            logger.error("Exception encountered while processing each heartbeat in the failure-detector.\n{}",
                    getFullStackTrace(e));
            return false;
        }
        return true;
    }

    /**
     * The messaging channel that buffers the incoming messages from
     * fellow members.
     * Typically execute in parallel to the membership protocol.
     */
    @Override
    public void msgInChannel() {
        if(!member.isFailed())
            msgService.receiveMessages();
    }

    /**
     * Sends heartbeats using any failure detector protocol.
     */
    public void sendHeartbeats() {

    }

    /**
     * Processes the received messages and
     */
    public void checkMessages() {
        logger.info("Receive heartbeats/messages");
        Set<MembershipMessage> messages = msgService.readMessages();
        if (messages != null) {
            for (MembershipMessage message : messages) {
                logger.trace("Message: {}", message);

                MessageType msgType = message.getMessageType();
                switch (msgType) {
                    case JOINREQ:
                        break;
                    case JOINREP:
                        break;
                    case GOSSIP_HEARTBEAT:
                        break;
                }
            }
        }
    }

    /**
     * Method to mark the current member as failed and terminate the process
     * i.e. exit the failure-detector-service.
     *
     * @return - service status.
     */
    @Override
    public boolean exit() {
        logger.info("Exiting the Failure Detector");
        try {

            // Sending a exit message to all neighbors.

            // Closing the Network Service
            msgService.stopService();

        } catch (Exception e){
            logger.error("Exception encountered while exiting.\n{}", getFullStackTrace(e));
            return false;
        }
        return true;
    }

    /**
     * Methods that compares the current member to the introducer
     * @return - true if the member is the introducer also.
     */
    public boolean isIntroducer(){
        return memberProcess.equals(introducer);
    }
}
