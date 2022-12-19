package edu.dkv.internal.common;

public class Constants {

    // Logging related constants
    public static final String SERVER_HOME                             = System.getenv("SERVER_HOME");
    public static final String SERVER_ID                               = System.getenv("SERVER_ID");
    public static final String LOG_LEVEL                               = System.getenv("SERVER_LOG_LEVEL");
    public static final String LOG_FILE_DIRECTORY                      = "log";
    public static final String LOG_FILE_NAME                           = "replica";
    public static final String LOG_FILE_SUFFIX                         = ".log";
}
