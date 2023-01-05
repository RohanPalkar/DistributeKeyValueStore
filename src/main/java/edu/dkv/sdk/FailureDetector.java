package edu.dkv.sdk;

public interface FailureDetector {

    void introduceSelfToGroup();

    void sendHeartbeats();

    void receiveHeartbeats();
}
