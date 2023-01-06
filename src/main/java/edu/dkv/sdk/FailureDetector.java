package edu.dkv.sdk;

import java.util.concurrent.atomic.AtomicBoolean;

public interface FailureDetector {

    /**
     * Primary implementation method for running the failure detector.
     * @return - status of the operation
     */
    boolean runFailureDetector();
}
