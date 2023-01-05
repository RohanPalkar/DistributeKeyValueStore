package edu.dkv.internal.service;

import edu.dkv.internal.entities.Member;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.sdk.FailureDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class GossipApplicationProcess implements Supplier<Boolean> {

    private final static Logger logger = LogManager.getLogger(GossipApplicationProcess.class);

    private final FailureDetector failureDetector;
    private final UserProcess memberProcess;
    private final UserProcess introducerProcess;

    private final Member member;

    public GossipApplicationProcess(int index, int introducerId) {
        this.memberProcess = new UserProcess(index, 0);
        this.introducerProcess = new UserProcess(introducerId, 0);
        this.failureDetector = new GossipFailureDetector(new GossipMessageService(memberProcess), introducerProcess);
        this.member = null;
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public Boolean get() {
        return null;
    }

    public static class SendGossipTask implements Runnable {

        private final FailureDetector failureDetector;

        public SendGossipTask(FailureDetector failureDetector) {
            this.failureDetector = failureDetector;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

        }
    }

    public static class ReceiveGossipTask implements Runnable {

        private final FailureDetector failureDetector;

        public ReceiveGossipTask(FailureDetector failureDetector) {
            this.failureDetector = failureDetector;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

        }
    }
}
