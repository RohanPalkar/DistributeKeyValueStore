package edu.dkv.app;

import edu.dkv.internal.common.Utils;
import edu.dkv.internal.config.AppConfig;
import edu.dkv.internal.config.ProcessConfig;
import edu.dkv.internal.entities.UserProcess;
import edu.dkv.internal.service.MessageService;
import edu.dkv.sdk.KVStoreApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@PropertySource("classpath:application.properties")
public class ProcessBasedApplication implements KVStoreApplication {

    private final static Logger logger = LogManager.getLogger(ProcessBasedApplication.class);

    private final int processCount;
    private final ProcessConfig processConfig;
    private final int msgReceiveTimeOutInSecs;
    private final ExecutorService executorService;
    private final int[] portsList;
    private List<UserProcess> processes;

    @Autowired
    public ProcessBasedApplication(AppConfig appConfig){ //@Value("${process.count}") int processCount,
        this.processConfig = appConfig.processConfig();
        this.processCount = processConfig.getCount();
        this.msgReceiveTimeOutInSecs = appConfig.processConfig().getMsgReceiveTimeoutInSecs();
        this.executorService = Executors.newFixedThreadPool(processConfig.getMaxThreads());
        this.portsList = new int[processCount];

        // Populating the port list with the ports sequentially, starting from the PORT_RANGE_START
        IntStream.range(0, processCount)
                .forEach(i -> portsList[i] = processConfig.getPortRangeStart() + i);
    }

    /**
     * Runs the Distributed Key-Value Store Application.
     */
    @Override
    public void run() {
        logger.info("Running application");
        logger.debug("Initiating process introduction...");
        List<CompletableFuture<UserProcess>> processesFuture =
                IntStream
                    .range(0, processCount)
                    .mapToObj(i -> CompletableFuture.supplyAsync(new InitProcess(i + 1, portsList[i]), executorService))
                    .collect(Collectors.toList());

        this.processes = processesFuture.stream()
                                            .map(CompletableFuture::join)
                                            .collect(Collectors.toList());
        logger.debug("{} processes initiated", processes.size());
        logger.trace("Processes: \n{}", processes);

        List<CompletableFuture<String>> messageFutures = processes.stream()
                    .map(p -> CompletableFuture.supplyAsync(new MessageTask(p, processes, msgReceiveTimeOutInSecs), executorService))
                    .collect(Collectors.toList());

        String allMessages = messageFutures
                                .stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.joining("\n"));

        logger.info("All Messages: \n{}", allMessages);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public static class InitProcess implements Supplier<UserProcess> {
        private final int port;
        private final int index;

        public InitProcess(int index, int port) {
            this.port = port;
            this.index = index;
        }

        @Override
        public UserProcess get() {
            UserProcess process = new UserProcess(index, port);
            logger.info("Introducing process : {}", process);
            return process;
        }
    }

    public static class MessageTask implements Supplier<String> {
        private final UserProcess process;
        private final List<UserProcess> processes;
        private final int msgReceiveTimeOutInSecs;

        public MessageTask(UserProcess process, List<UserProcess> processes, int msgReceiveTimeOutInSecs) {
            this.process = process;
            this.processes = processes;
            this.msgReceiveTimeOutInSecs = msgReceiveTimeOutInSecs;
        }

        @Override
        public String get() {
            ThreadContext.push(process.getProcessName());
            MessageService messageService = new MessageService(process);

            // Finding a random target process to send the message to.
            UserProcess destProcess = Utils.findRandomProcess(process, processes);

            // Constructing the message.
            String message = "Hello from " + process.getProcessName() + " to " + destProcess.getProcessName();

            // Sending the message.
            messageService.sendMessage(message, destProcess);

            // Crucial, This sleep offers the application to send all messages and
            // thus below wait on receiving messages may work out. NOT in real application
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Receiving the messages with a time out.
            String messages = String.join(" ; ", messageService.receiveMessages(msgReceiveTimeOutInSecs));
            logger.debug("Received Messages: {}", messages);

            // Stopping the MessageService and releasing the sockets.
            messageService.stopMsgService();

            ThreadContext.pop();
            return messages;
        }
    }
}
