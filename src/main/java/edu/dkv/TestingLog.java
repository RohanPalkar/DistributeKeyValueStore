package edu.dkv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

public class TestingLog {

    private final static Logger logger = LogManager.getLogger(TestingLog.class);

    public static void main(String[] args)  {
        Runnable r = () -> {
            logger.info("Hello, test at INFO");
            logger.debug("Hello, test at DEBUG");
            logger.trace("Hello, test at TRACE");
            logger.warn("Hello, test at WARN");
            logger.error("Hello, test at ERROR");
            logger.fatal("Hello, test at FATAL");
        };
        IntStream.range(0,5).forEach(i -> new Thread(r).start());
    }


}
