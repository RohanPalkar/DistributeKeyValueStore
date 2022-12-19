package edu.dkv.internal.network;

import edu.dkv.exceptions.ApplicationBufferOverflowException;
import edu.dkv.internal.common.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.dkv.internal.common.Constants.APP_BUFFER_SIZE;

public class ApplicationBuffer {

    private final static Logger logger = LogManager.getLogger(ApplicationBuffer.class);

    private final byte[] applicationBuffer = new byte[APP_BUFFER_SIZE];
    private AtomicInteger offset = new AtomicInteger(0);

    private final Queue<String> msgQueue = new LinkedList<>();

    public void addToBuffer(byte[] buffer, int bufferLength) throws ApplicationBufferOverflowException {
        final int currBufferSize = offset.get();
        int expectedSize = currBufferSize + bufferLength;
        if(expectedSize > APP_BUFFER_SIZE){
            logger.error("Application buffer is either full or cannot add incoming message.");
            throw new ApplicationBufferOverflowException("Application buffer is either full or cannot accomodate more messages. Size: " + expectedSize);
        }

        // Adding to application buffer.
/*        for(int i = currBufferSize - 1, j = 0 ; i < currBufferSize - 1 + bufferLength ; i++, j++){
            applicationBuffer[i] = buffer[j];
        }*/
        offset.set(expectedSize);

        // Adding the incoming message to the msg-queue.
        String incomingMessage = new String(buffer, 0, bufferLength);
        logger.debug("Incoming Message: {}", incomingMessage);
        msgQueue.offer(incomingMessage);
    }

    public String readBuffer(){
        // Return type to be changed to the message-type
        // Handle concurrency, if buffer is being written to, then read should be delayed.
        String first = !msgQueue.isEmpty() ? msgQueue.peek() : "";
        if(first.length() != 0)
            offset.set(Math.max(offset.get() - first.length(), 0));
        return !msgQueue.isEmpty() ? msgQueue.poll() : null;
    }
}
