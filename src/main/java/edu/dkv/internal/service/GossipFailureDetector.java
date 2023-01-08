package edu.dkv.internal.service;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.GossipConfig;
import edu.dkv.internal.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static edu.dkv.internal.common.Utils.getFullStackTrace;
import static edu.dkv.internal.common.Utils.printMembershipList;

public class GossipFailureDetector extends AbstractFailureDetector {

    private final static Logger logger = LogManager.getLogger(GossipFailureDetector.class);

    private final int PING;
    private final long TFAIL;
    private final long TREMOVE;
    private final int KLIST;
    private final int GOSSIP;

    private final GossipMessageService msgService;
    private final UserProcess memberProcess;
    private final UserProcess introducer;
    private final Member member;
    private boolean shouldDeleteMember;
    private final List<EndPoint> failedMembers;

    public GossipFailureDetector(UserProcess memberProcess, UserProcess introducer, GossipConfig gossipConfig) {
        this.msgService = new GossipMessageService(memberProcess);
        this.memberProcess = memberProcess;
        this.introducer = introducer;
        this.member = new Member();
        this.failedMembers = new ArrayList<>();

        TFAIL = gossipConfig.getTFail();
        PING = (int) TFAIL;
        TREMOVE = gossipConfig.getTRemove();
        KLIST = gossipConfig.getKList();
        GOSSIP = gossipConfig.getGossip();
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
            member.setEndPoint(memberProcess.getEndPoint());
            member.setNeighborCount(0);
            member.setHeartbeat(0);
            member.setPingCounter(TFAIL);
            member.setTimeOutCounter(-1);

            // Initializing the member ship list entry with the current member node.
            MemberListEntry nodeEntry = new MemberListEntry(memberProcess.getEndPoint(), 0, System.currentTimeMillis());
            member.addToMembershipList(memberProcess.getEndPoint(), nodeEntry);
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
            logger.trace("Processing Heartbeats");

            // If the member has been marked failed, then it can no longer participate in
            // gossip protocol, hence it's marked for deletion.
            if(member.isFailed()){
                logger.error("Member node {} has failed", memberProcess.getProcessName());
                shouldDeleteMember = true;
                return false;
            }

            // Processes any messages present in the application buffer.
            checkMessages();

            // Only once a member is part of the group, it can sent heartbeats to other members.
            // If in the group, perform the gossip protocol duties.
            if(member.isInGroup()) {
                sendHeartbeats();
            } else {
                logger.error("Member node {} is still not part of the group. Can't send heartbeats", memberProcess.getProcessName());
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
     * Sends the heartbeat by selecting GOSSIP no. of random neighbours among
     * its membership list, after TFAIL time has elapsed.
     */
    public void sendHeartbeats() {
        logger.trace("Sending heartbeats...");

        // Detect any failures.
        detectFailure();

        // Incrementing the heartbeat of the current member
        member.incrementHeartbeat();
        logger.debug("Updated heartbeat: {}", member.getHeartbeat());

        // Updating the heartbeat of the current member in the membership list that
        // also contains the current member node.
        if(member.containsMemberListEntry(member.getEndPoint())){
            MemberListEntry selfEntry = member.getMemberListEntry(member.getEndPoint());
            if(selfEntry != null)
                selfEntry.setHeartbeat(member.getHeartbeat());
        }

        // Finding GOSSIP no. of neighbors within the list, other than the self node.
        Set<EndPoint> gossipNeighbors = findGossipNeighbors();

        if(!gossipNeighbors.isEmpty()) {
            // Iterating over each of the gossip neighbors to send the gossip/heartbeat messages
            MembershipMessage gossipMsg = MembershipMessage.createMessage()
                    .setMessageType(MessageType.GOSSIP_HEARTBEAT)
                    .setEndPoint(member.getEndPoint())
                    .setHeartbeat(member.getHeartbeat())
                    .setMembershipList(member.getMembershipListForMessage())
                    .build();
            logger.debug("Gossip Message to be sent to {} gossip-neighbors: \n{}",
                    gossipNeighbors.size(), gossipMsg);
            gossipNeighbors.forEach(gn -> msgService.sendMessage(gn, gossipMsg));
        }

        logger.trace("Heartbeat sending completed");


        // Setting the ping counter to PING again
        //member.setPingCounter(PING);
        // Decrementing the ping counter until it is 0. It is then reset with TFAIL value
        /*if(member.getPingCounter() == 0) {
            logger.trace("Ping counter is 0. Time to send heartbeats");
        } else {
            // Decrementing the counter... no action required as of now
            member.decrementPingCounter();
            logger.trace("{} - Periodic ping counter not there yet...skipping heartbeat sends", member.getPingCounter());
        }*/
    }

    /**
     * Identifies or detects the failed nodes based on the gossip protocol algorithm
     * Only the TFAIL and TREMOVE properties are used - No suspicion mechanism.
     */
    void detectFailure(){
        logger.trace("Initiating failure detection");
        final long failTime = TFAIL * 1000L;
        final long removeTime = TREMOVE * 1000L;

        // Iterating over the membership list and detecting failures.
        Set<EndPoint> newlyFailedMembers = new HashSet<>();
        member.getMembershipList().forEach((k,v) -> {
            long nodeTimeStamp = v.getTimestamp();

            // If the currentTime (T) - initial ping wait time (TFAIL) - overall wait time (TREMOVE)
            // is greater than the last recorded local time stamp of the id, then mark it fail.
            long failureDetectionTime = System.currentTimeMillis() - failTime - removeTime;
            if(!k.equals(member.getEndPoint()) && failureDetectionTime > nodeTimeStamp){
                logger.trace("Member: {} has failed as \nFailure-Detection-Time: {}\nLast Updated Node Timestamp: {}",
                        k, failureDetectionTime, nodeTimeStamp);
                newlyFailedMembers.add(k);
            }
        });

        if(!newlyFailedMembers.isEmpty()) {
            logger.debug("New Failed Members: {}", newlyFailedMembers);

            // Removing the failed members from the existing membership list.
            newlyFailedMembers.forEach(f -> member.getMembershipList().remove(f));

            // Adding the local failed members to the overall failed list
            failedMembers.addAll(new ArrayList<>(newlyFailedMembers));
            logger.trace("All failed members: {}", failedMembers);
        }
        logger.trace("Failed detection completed");
    }

    /**
     * Finds all the neighbors of the current member node to which
     * the heartbeat would be gossiped. Selects GOSSIP # of random neighbors from
     * the membership list (excluding itself)
     * @return set of member neighbors
     */
    Set<EndPoint> findGossipNeighbors(){
        logger.trace("Preparing the list of gossip-neighbors");
        // Maintaining a unique collection of gossip-neighbors.
        Set<EndPoint> gossipNeighbors = new HashSet<>();

        // Checking if the list is empty or not, otherwise if it's not empty, can't gossip.
        if(!member.getMembershipList().isEmpty()){
            EndPoint currentMemberNode = member.getEndPoint();
            Set<EndPoint> neighbors = member.getMembershipList().keySet();

            // If the no. of neighbors is less than the GOSSIP count, then prepare all of them
            // as gossip-neighbors excluding the current member node.
            if(neighbors.size() + 1 <= GOSSIP){
                gossipNeighbors = neighbors.stream()
                        .filter(n -> !n.equals(currentMemberNode))
                        .collect(Collectors.toSet());

                if(gossipNeighbors.size() > 0)
                    logger.trace("Minimal gossip neighbors : {}\n{}", gossipNeighbors.size(), gossipNeighbors);
                else
                    logger.warn("No gossip neighbors found");
            } else {

                // Iterating over the list until we get all unique GOSSIP no. of neighbors.
                // Since we know that the neighbors size is more than GOSSIP, we should get
                // GOSSIP unique members.
                List<EndPoint> neighborsList = new ArrayList<>(neighbors);
                while(gossipNeighbors.size() != GOSSIP){

                    // Generating random index
                    int randomIndex = Utils.getRandomNumber(0, gossipNeighbors.size());
                    EndPoint randomMember = neighborsList.get(randomIndex);

                    // Checking if the random index is unique and adding the corresponding the random member
                    // to the gossip neighbors list only if it hasn't been added already and it is not equal
                    // to the current member node.
                    if(!gossipNeighbors.contains(randomMember) && !randomMember.equals(member.getEndPoint()))
                        gossipNeighbors.add(randomMember);
                }

                if(gossipNeighbors.size() > 0)
                    logger.trace("Full gossip neighbors: {}\n{}", gossipNeighbors.size(), gossipNeighbors);
                else
                    logger.warn("No gossip neighbors found");
            }
        } else
            logger.debug("Membership list for {} is empty. Can't Gossip", member.getEndPoint());

        return gossipNeighbors;
    }

    /**
     * Processes the received messages and based on the individual message type,
     * appropriate handling/processing of the messages is done for each of the
     * messages queued in the application buffer of the current member node.
     */
    public void checkMessages() {
        logger.trace("Checking heartbeats/messages");
        Set<MembershipMessage> messages = msgService.readMessages();
        if (messages != null) {
            for (MembershipMessage message : messages) {
                switch (message.getMessageType()) {
                    case JOINREQ:
                        processJoinReq(message);
                        break;
                    case JOINREP:
                        processJoinRep(message);
                        break;
                    case GOSSIP_HEARTBEAT:
                        processGossip(message);
                        break;
                }
            }
        }
        logger.trace("Heartbeat message processing completed");
    }

    /**
     * Processes a JOIN_REQ (Join Request) message that a member node that
     * joined the group, sent to the current member node (or the introducer
     * in this case).
     *
     * The source member node details are updated in the membership list of the
     * introducer and a JOIN_REP (Join Response) message is sent by the introducer
     * to the source member node with the updated membership list.
     *
     * NOTE: This JOIN_REQ message is processed only by the introducer and not
     * other member nodes.
     *
     * @param message - {@code MembershipMessage} Join-Req Message
     */
    void processJoinReq(MembershipMessage message){
        logger.trace("Processing Join REQ Message: {}", message);

        // Adding the member node that has just joined the group  in the membership list with the time stamp of the
        // current member node (i.e. the introducer)
        MemberListEntry entry =
                new MemberListEntry(message.getEndPoint(), message.getHeartbeat(), System.currentTimeMillis());
        logger.trace("Adding the entry: {}", entry);

        // To maintain a partial membership list, restricting the size of the list to the K value i.e. KLIST specified
        // in the application.properties. If the list is full, then a random index in the list is selected and updated
        // with the target node, else the target node is simply added.
        Set<EndPoint> existingMembers = member.getMembershipList().keySet();
        if(existingMembers.size() > KLIST)
            removeRandomMember(existingMembers);
        member.addToMembershipList(message.getEndPoint(), entry);
        logger.trace("Updated membership-list: {}", printMembershipList(member.getMembershipList()));

        // The current node (introducer in this case) sends a JOINREP Membership message to the member node
        // that joined the group earlier by sending a JOINREQ.
        MembershipMessage joinRepMsg = MembershipMessage.createMessage()
                .setMessageType(MessageType.JOINREP)
                .setEndPoint(member.getEndPoint())
                .setHeartbeat(member.getHeartbeat())
                .setMembershipList(member.getMembershipListForMessage())
                .build();
        logger.trace("Attempting to send JOIN_REP Msg: {}", joinRepMsg);

        msgService.sendMessage(message.getEndPoint(), joinRepMsg);
        logger.trace("JOINREQ message processing completed");
    }

    /**
     * Processes a JOIN_REP (Join Response) message from the introducer node and
     * updates the membership list at current node from the membership list sent
     * by the introducer node.
     *
     * NOTE: This JOIN_REP message is processed by all other nodes, other than the
     * introducer as the introducer simply add itself to the group in the
     * 'introduceSelfToGroup' method implementation.
     *
     * @param message - {@code MembershipMessage} Join-Rep Message
     */
    void processJoinRep(MembershipMessage message){
        logger.trace("Processing Join REP Message: {}", message);

        // Since the node has now joined the group as it has received a JOIN_REP, marking node as inGroup
        member.setInGroup(true);

        // Membership list as sent by the introducer. It's expected, atleast intially that the introducer
        // will have the most updated/recent membership list as all other nodes communicate with the
        // introducer first. Thus, updating the membership list at the current node (non-introducer) from
        // the list received from the introducer.
        Set<MemberListEntry> sourceList = message.getMembershipList();
        logger.trace("Received source-list from introducer: {}", printMembershipList(sourceList));

        // Iterates through the source list and adds the missing node to the current member node's
        // membership list if the node to be added doesn't already exist in the list and that it is
        // not part of the failed members list.
        sourceList.forEach(e -> {
           EndPoint endPoint = e.getEndPoint();
           if(! member.containsMemberListEntry(endPoint) && !failedMembers.contains(endPoint)){
               if(member.getMembershipList().size() > KLIST)
                   removeRandomMember(member.getMembershipList().keySet());

               logger.trace("Adding {} to membership list", endPoint);
               member.addToMembershipList(endPoint, e);
           }
        });
        logger.trace("JOINREP Message processing completed");
    }

    /**
     * Gossip Protocol Implementation
     * Upon receiving the gossip heartbeats from the member-nodes, the membership-lists
     * are tallied and merged according to the gossip protocol algorithm.
     *
     * @param message - {@code MembershipMessage} Join-Rep Message
     */
    void processGossip(MembershipMessage message){
        logger.trace("Processing Gossip Message: {}", message);

        if(!member.isInGroup()){
            logger.warn("Member node {} is still not part of the group. Can't receive/process heartbeats - msg dropped", memberProcess.getProcessName());
            return;
        }

        // The membership list at the current member node and the one received from the
        // member node as part of Gossip Message is merged together according to the
        // Gossip Style Membership protocol implementation.
        Set<MemberListEntry> sourceList = message.getMembershipList();

        // Because partial membershiplist is maintained, it is possible that the node
        // that gossiped the list, it's own entry is not present in the membership list.
        // Thus, updating the source list with the sender if not present. Essentially making
        // the list as K + 1
        Optional<MemberListEntry> oe = sourceList.stream()
                .filter(s -> s.getEndPoint().equals(message.getEndPoint()))
                .findFirst();
        if(!oe.isPresent()){
            logger.debug("Sender's own entry not present in the membership list. Adding it now...");
            MemberListEntry senderOwnEntry =
                    new MemberListEntry(message.getEndPoint(), message.getHeartbeat(), System.currentTimeMillis());
            sourceList.add(senderOwnEntry);
        }

        // Current Member Node list
        Map<EndPoint, MemberListEntry> currentList = member.getMembershipList();

        // Iterates over the source list and matches entry with the current member node
        // list and applies the protocol logic.
        sourceList.forEach(sourceEntry -> {
            EndPoint e1 = sourceEntry.getEndPoint();

            // ## Performing the Gossip Protocol ##
            // (a) If the node from the gossiped list exists in the current member node
            // list & has not failed (i.e. not present in the failed member list):
            //
            // > If the heartbeat of the node in the gossip-ed list received is higher
            // than the heartbeat of the same node in the current member node list,
            // then the heartbeat in the current member-node list is updated with the
            // gossiped-heartbeat while the time-stamp is updated with the current
            // timestamp on the current member node.
            //
            // (b) If the node from the gossiped list has not failed :
            // > It is added to the membership list at the current member node.
            //
            if(currentList.containsKey(e1) && !failedMembers.contains(e1)) {
                MemberListEntry existingEntry = currentList.get(e1);

                // If received heartbeat is greater than the existing heartbeat.
                if(sourceEntry.getHeartbeat() > existingEntry.getHeartbeat()){

                    // Updating the received heartbeat
                    existingEntry.setHeartbeat(sourceEntry.getHeartbeat());
                    // Updating the current node time-stamp.
                    existingEntry.setTimestamp(System.currentTimeMillis());

                    logger.trace("Updated Heartbeat entry: {}", existingEntry);
                }
            } else if(!failedMembers.contains(e1)) {
                if(currentList.size() > KLIST)
                    removeRandomMember(currentList.keySet());

                logger.trace("Adding {} to membership list", e1);
                currentList.put(e1, sourceEntry);
            }
        });
        logger.trace("GOSSIP message processing completed.");
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

            // Clearing the failed member's list
            failedMembers.clear();

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

    /**
     * If the size of the membership list exceeds the fixed K size captured by the property KLIST,
     * then a random node is selected amongst the existing list and removed so that the newer
     * endpoint can be added.
     *
     * @param existingMembers - set of endpoints.
     */
    private void removeRandomMember(Set<EndPoint> existingMembers){
        int randomIndex = Utils.getRandomNumber(0, existingMembers.size() - 1);

        EndPoint replacedEndpoint = null;

        Iterator<EndPoint> i = existingMembers.iterator();
        int counter = 0;
        while(i.hasNext()){
            replacedEndpoint = i.next();
            if(counter == randomIndex)
                break;
            ++counter;
        }

        logger.trace("Removing random endpoint to maintain K size: {}", replacedEndpoint);
        member.getMembershipList().remove(replacedEndpoint);
    }
}
