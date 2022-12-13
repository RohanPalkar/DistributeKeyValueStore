package edu.dkv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestingLog {

    private final static Logger logger = LogManager.getLogger(TestingLog.class);

    public static void main(String[] args) {
        logger.info("Hello, test at INFO");
        logger.debug("Hello, test at DEBUG");
        logger.trace("Hello, test at TRACE");
    }
}
