package edu.dkv.internal.common;

import edu.dkv.app.ProcessBasedApplication;
import edu.dkv.internal.config.ProcessConfig;
import edu.dkv.internal.entities.EndPoint;
import edu.dkv.internal.entities.MemberListEntry;
import edu.dkv.internal.entities.UserProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utils {

    private final static Logger logger = LogManager.getLogger(Utils.class);

    public static String getFullStackTrace(Exception e){
        OutputStream os = new OutputStream() {

            private StringBuilder sb = new StringBuilder();

            @Override
            public void write(int i) throws IOException {
                this.sb.append((char)i);
            }

            public String toString(){
                return this.sb.toString();
            }
        };

        PrintWriter pw = new PrintWriter(os);
        e.printStackTrace(pw);
        pw.close();

        return os.toString();
    }

    public static int getRandomNumber(int min, int max) {
        return new Random()
                .ints(min, max)
                .findFirst()
                .getAsInt();
    }


    public static UserProcess findRandomProcess(UserProcess currentProcess, List<UserProcess> processes){
        int randomIndex = Utils.getRandomNumber(0, processes.size() - 1);
        logger.trace("First Random Index: {}", randomIndex);
        UserProcess targetProcess = processes.get(randomIndex);

        while(targetProcess.equals(currentProcess)){
            randomIndex = Utils.getRandomNumber(0, processes.size() - 1);
            logger.trace("Subsequent Random Index: {}", randomIndex);
            targetProcess = processes.get(randomIndex);
        }
        logger.debug("Current Process: {}, \nTarget Process: {}", currentProcess, targetProcess);
        return targetProcess;
    }

    public static int getPort(ProcessConfig processConfig){
        int portMin = processConfig.getPortRangeStart();
        int portMax = processConfig.getPortRangeStop();
        int range = portMax - portMin;
        int port = getRandomPortWithinRange(portMin, portMax);
        while( port == -1 && range > 0 ){
            --range;
            port = getRandomPortWithinRange(portMin, portMax);
        }
        return port;
    }

    public static int getRandomPortWithinRange(int portMin, int portMax){
        int randomPort = Utils.getRandomNumber(portMin, portMax);
        logger.trace("Random port selected within [{}, {}]: {}", portMin, portMax, randomPort);

        ServerSocket serverSocket = null;
        DatagramSocket datagramSocket = null;
        try {
            serverSocket = new ServerSocket(randomPort);
            datagramSocket = new DatagramSocket(randomPort);
            return randomPort;
        } catch (IOException e) {
            if(e.getMessage().contains("Address already in use"))
                logger.debug("Port {} already in use !!!", randomPort);
            else
                logger.trace("Issue encountered while checking for available port: {}", getFullStackTrace(e));
        } finally {
            if(datagramSocket != null)
                datagramSocket.close();

            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }

        return -1;
    }

    /**
     * Shuts down an {@code ExecutorService} instance gracefully while checking for
     * any queue-ed up processes to complete/cancel also.
     *
     * @param executor - {@code ExecutorService} instance
     */
    public static void shutDownExecutor(ExecutorService executor){
        logger.trace("Shutting down executor services");
        logger.trace("Executor status before shutdown {}", executor.toString());
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            if(!executor.shutdownNow().isEmpty())
                logger.warn("Warning: Few tasks are still running for the primary executor. Ignoring them.");
        } catch (InterruptedException e1) {
            if(!executor.shutdownNow().isEmpty())
                logger.warn("Warning: Few tasks are still running for the primary executor. Ignoring them.");
        }
        logger.trace("Executor status after shutdown {}", executor.toString());
    }

    /**
     * Shuts down a {@code ScheduledThreadPoolExecutor} instance gracefully while
     * making sure that any scheduled tasks are cleared and terminated.
     *
     * @param scheduler - {@code ScheduledThreadPoolExecutor} instance
     */
    public static void shutDownScheduler(ScheduledThreadPoolExecutor scheduler){
        logger.trace("Shutting down scheduler services");
        logger.trace("Scheduler status before shutdown {}", scheduler.toString());
        scheduler.shutdown();
        try {
            scheduler.getQueue().clear();
            scheduler.awaitTermination(100,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            scheduler.shutdownNow();
        }
        logger.trace("Scheduler status after shutdown {}", scheduler.toString());

        if(!scheduler.shutdownNow().isEmpty())
            logger.warn("Warning: Few tasks are still running for the scheduler service. Ignoring them.");
    }

    /**
     * Shuts down a {@code ScheduledExecutorService} instance gracefully while
     * making sure that any scheduled tasks are cleared and terminated.
     *
     * @param executor - {@code ScheduledExecutorService} instance
     */
    public static void shutDownScheduler(ScheduledExecutorService executor){
        logger.trace("Shutting down ScheduledExecutorService services");
        logger.trace("ScheduledExecutorService status before shutdown {}", executor.toString());
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            if(!executor.shutdownNow().isEmpty())
                logger.warn("Warning: Few tasks are still running for the primary executor. Ignoring them.");
        } catch (InterruptedException e1) {
            if(!executor.shutdownNow().isEmpty())
                logger.warn("Warning: Few tasks are still running for the primary executor. Ignoring them.");
        }
        logger.trace("ScheduledExecutorService status after shutdown {}", executor.toString());
    }

    public static String printMembershipList(Map<EndPoint, MemberListEntry> membershipList){
        if(membershipList == null || membershipList.isEmpty())
            return "";

        return printMembershipList(new HashSet<>(membershipList.values()));
    }

    public static String printMembershipList(Set<MemberListEntry> membershipList){
        if(membershipList == null || membershipList.isEmpty())
            return "";

        StringBuffer sb = new StringBuffer();
        sb.append("\n{\n");
        membershipList
                .forEach(v -> sb.append(" ")
                                .append(v)
                                .append("\n"));
        sb.append("}");
        return sb.toString();
    }

}
