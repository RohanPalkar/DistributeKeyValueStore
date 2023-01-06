package edu.dkv.internal.service;

public abstract class AbstractFailureDetector {

    /**
     * Initializes the member node
     *
     * @return - true if init succeeded.
     */
    protected abstract boolean initNode();

    /**
     * A member process is introduced to group of members that run another
     * instance of the Failure Detector.
     *
     * @return - true if introduction succeeded.
     */
    protected abstract boolean introduceSelfToGroup();

    /**
     * Primary method for performing the membership protocol duties.
     *
     * @return - true if all operations succeeded
     */
    protected abstract boolean processHeartbeats();

    /**
     * The messaging channel that buffers the incoming messages from
     * fellow members.
     * Typically execute in parallel to the membership protocol.
     */
    protected abstract void msgInChannel();

    /**
     * Method to mark the current member as failed and terminate the process
     * i.e. exit the failure-detector-service.
     *
     * @return - service status.
     */
    protected abstract boolean exit();
}
