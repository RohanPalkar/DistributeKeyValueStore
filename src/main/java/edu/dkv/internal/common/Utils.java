package edu.dkv.internal.common;

import edu.dkv.app.ProcessBasedApplication;
import edu.dkv.internal.entities.UserProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

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


}
