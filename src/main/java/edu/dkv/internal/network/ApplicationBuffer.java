package edu.dkv.internal.network;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class ApplicationBuffer<T> {

    private final static Logger logger = LogManager.getLogger(ApplicationBuffer.class);

    /**
     * The non-blocking, unbounded, thread safe (lock free) queue for caching the
     * incoming data. This queue acts as the application buffer for the network
     * layer exchanging the messages. This class is generified to accomodate
     * serialization and deserialization of byte[] into objects are required
     * by the application.
     */
    private final ConcurrentLinkedQueue<T> dataQueue = new ConcurrentLinkedQueue<>();

    public void buffer(byte[] bufferData, int bufferLength)  {
        logger.trace("Adding {} byte data to application buffer queue.", bufferLength);
        dataQueue.add((T) SerializationUtils.deserialize(bufferData));
    }

    public T readBuffer(){
        return !dataQueue.isEmpty() ? dataQueue.poll() : null;
    }

    public boolean isEmpty(){
        return dataQueue.isEmpty();
    }
}
