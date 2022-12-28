package edu.dkv.internal.service;

import edu.dkv.app.ProcessBasedApplication;
import edu.dkv.internal.common.Utils;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.DatagramService;
import edu.dkv.sdk.NetworkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MessageService {

    private final static Logger logger = LogManager.getLogger(MessageService.class);

    private final UserProcess process;
    private final NetworkService network;
    private final ApplicationBuffer<String> appBuffer;

    public MessageService(UserProcess process) {
        this.process = process;
        this.appBuffer = new ApplicationBuffer<String>();
        this.network = new DatagramService(process.getEndPoint().getPort(), appBuffer);
        this.network.start();
    }

    public void sendMessage(String message, UserProcess destinationProcess){
        logger.debug("Sending message from {}:{}", process.getEndPoint().getAddress(), process.getEndPoint().getPort());
        network.sendMessages(destinationProcess.getEndPoint(), message);
    }

    public List<String> receiveMessages(Integer timeOutInSecs){
        List<String> messages = new ArrayList<>();
        while(timeOutInSecs > 0){
            network.receiveMessages();
            if(!appBuffer.isEmpty())
                messages.add(appBuffer.readBuffer());

            try {
                // Adding a random time delay, max: 200 milli seconds.
                Thread.sleep(Utils.getRandomNumber(1, 200));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            --timeOutInSecs;
        }

        return messages;
    }

    public void stopMsgService(){
        this.network.stop();
    }
}
