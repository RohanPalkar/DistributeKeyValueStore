package edu.dkv.sdk;

import java.util.concurrent.atomic.AtomicBoolean;

public interface FailureDetector {

    /**
     * Initializes the member node
     *
     * @return - true if init succeeded.
     */
    boolean initNode();

    /**
     * A member process is introduced to group of members that run another
     * instance of the Failure Detector.
     *
     * @return - true if introduction succeeded.
     */
    boolean introduceSelfToGroup();

    /**
     * Sends heartbeats using any failure detector protocol.
     */
    void sendHeartbeats();

    /**
     * Receives heartbeats using any failure detector protocol.
     */
    void receiveHeartbeats();

    /**
     * Method to mark the current member as failed and terminate the process
     * i.e. exit the failure-detector-service.
     *
     * @return - service status.
     */
    boolean exit();
}
