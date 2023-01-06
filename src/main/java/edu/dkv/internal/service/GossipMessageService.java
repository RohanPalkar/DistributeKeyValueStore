package edu.dkv.internal.service;

import edu.dkv.internal.entities.EndPoint;
import edu.dkv.internal.entities.MembershipMessage;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.DatagramService;
import edu.dkv.sdk.NetworkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GossipMessageService {

    private final static Logger logger = LogManager.getLogger(GossipMessageService.class);

    private final UserProcess process;
    private final ApplicationBuffer<MembershipMessage> appBuffer;
    private final NetworkService network;

    public GossipMessageService(UserProcess process) {
        this.process = process;
        this.appBuffer = new ApplicationBuffer<>();
        this.network = new DatagramService(process.getEndPoint().getPort(), appBuffer);
    }

    public void sendMessage(EndPoint destTarget, MembershipMessage message){
        network.sendMessages(destTarget, message);
    }
}
