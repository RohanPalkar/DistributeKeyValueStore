package edu.dkv.internal.service;

import edu.dkv.internal.entities.EndPoint;
import edu.dkv.internal.entities.MembershipMessage;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.DatagramService;
import edu.dkv.sdk.NetworkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GossipMessageService {

    private final static Logger logger = LogManager.getLogger(GossipMessageService.class);

    private final UserProcess process;
    private final ApplicationBuffer<MembershipMessage> appBuffer;
    private final NetworkService network;

    public GossipMessageService(UserProcess process) {
        this.process = process;
        this.appBuffer = new ApplicationBuffer<>();
        this.network = new DatagramService<MembershipMessage>(process.getEndPoint().getPort(), appBuffer);
        this.network.start();
    }

    /**
     * Sends a {@code MembershipMessage} message to the target node via the
     * network service.
     * @param destTarget        - destination node/process
     * @param message           - membership message
     */
    public void sendMessage(EndPoint destTarget, MembershipMessage message){
        network.sendMessages(destTarget, message);
    }

    /**
     * Receives the messages from the network service and adds them to the
     * application buffer.
     */
    public void receiveMessages(){
        network.receiveMessages();
    }

    /**
     * Reads the {@code MembershipMessage} messages from the application
     * buffer and returns a collection
     * @return - membership messages.
     */
    public synchronized Set<MembershipMessage> readMessages(){
        Set<MembershipMessage> messages = new HashSet<>();
        while(!appBuffer.isEmpty()){
            MembershipMessage msg = appBuffer.readBuffer();
            if(msg != null)
                messages.add(msg);
        }
        logger.debug("{} messages received from app-buffer", messages.size());
        return messages;
    }

    /**
     * Stops the messaging service by stopping the network service.
     * Closes the socket connections appropriately.
     */
    public void stopService(){
        network.stop();
    }
}
