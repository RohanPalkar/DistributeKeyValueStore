package edu.dkv.test;

import edu.dkv.internal.network.ApplicationBuffer;
import edu.dkv.internal.network.DatagramService;
import edu.dkv.internal.network.EndPoint;
import edu.dkv.sdk.NetworkService;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NetworkServiceTest {

    private ApplicationBuffer buffer2 = new ApplicationBuffer();
    private NetworkService sourceNode = new DatagramService(3000, new ApplicationBuffer());
    private NetworkService destNode = new DatagramService(4000, buffer2);

    @BeforeAll
    public void setup(){
        sourceNode.start();
        destNode.start();
    }

    @Test
    public void clientTest() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        sourceNode.sendMessage(new EndPoint(address, 4000), "hello");

        buffer2 = destNode.receiveMessage();
        assertEquals("hello", buffer2.readBuffer());
    }

    @AfterAll
    public void teardown(){
        sourceNode.stop();
        destNode.stop();
    }
}
