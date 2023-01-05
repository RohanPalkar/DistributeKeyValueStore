package edu.dkv.internal.common;

public class Constants {

    // Logging related constants
    public static final String SERVER_HOME                             = System.getenv("SERVER_HOME");
    public static final String SERVER_ID                               = System.getenv("SERVER_ID");
    public static final String LOG_LEVEL                               = System.getenv("SERVER_LOG_LEVEL");
    public static final String PROCESS_LOGGER                          = System.getenv("PROCESS_LOGGER");
    public static final String LOG_FILE_DIRECTORY                      = "log";
    public static final String LOG_FILE_NAME                           = "replica";
    public static final String LOG_FILE_SUFFIX                         = ".log";

    // Network layer configuration.
    public static final int UDP_SOCKET_TIMEOUT                         = 100;
    public static final int APP_BUFFER_SIZE                            = 30000;
    public static final int MAX_MSG_SIZE                               = 4000;

    // Process configuration
    public static final int DEFAULT_MAX_THREADS                        = 10;
    public static final String PROCESS_NAME_PREFIX                     = "process_";
}
