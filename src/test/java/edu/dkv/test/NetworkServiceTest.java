package edu.dkv.test;

import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.DatagramService;
import edu.dkv.internal.network.EndPoint;
import edu.dkv.sdk.NetworkService;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NetworkServiceTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    /*@BeforeAll
    public void setup(){
        sourceNode.start();
        destNode.start();
    }*/

    @Test
    public void clientTest() throws UnknownHostException, ExecutionException, InterruptedException {
        Future<?> f1 = sendMessage( "hello", 3000, 4000);
        Future<ApplicationBuffer> f2 = receiveMessage(4000);

        while(!(f1.isDone() && f2.isDone())){
            System.out.println("Waiting for messages to be exchanged");
            Thread.sleep(500);
        }

        Assertions.assertEquals("hello", f2.get().readBuffer());
    }

    private Future<?> sendMessage(String message, int sourceport, int destport){
        return executorService.submit(() -> {
            NetworkService sourceNode = new DatagramService(sourceport);
            sourceNode.start();
            InetAddress address = null;
            try {
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            sourceNode.sendMessage(new EndPoint(address, destport), message);
            sourceNode.stop();
        });
    }

    private Future<ApplicationBuffer> receiveMessage(int port){
        return executorService.submit(() -> {
            NetworkService destNode = new DatagramService(port, new ApplicationBuffer());
            destNode.start();
            ApplicationBuffer buffer = destNode.receiveMessage();
            destNode.stop();
            return buffer;
        });
    }

    /*@AfterAll
    public void teardown(){
        sourceNode.stop();
        destNode.stop();
    }*/
}
