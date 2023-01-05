package edu.dkv.internal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    @ConfigurationProperties(prefix = "process")
    public ProcessConfig processConfig(){
        return new ProcessConfig();
    }

    @Value("${maxRunningTimeInMinutes}")
    private Integer maxRunningTimeInMinutes;

    @Value("${maxRunningTimeInSecs}")
    private Integer maxRunningTimeInSecs;

    public long getRunningTime() {
        if(maxRunningTimeInMinutes != null)
            return maxRunningTimeInMinutes * 60 * 1000;
        if(maxRunningTimeInSecs != null)
            return maxRunningTimeInSecs * 1000;
        return -1;
    }

    @Override
    public String toString() {
        return "AppConfig {" +
                "\nmaxRunningTime (long)=" + getRunningTime() +
                "\nprocessConfig=" + processConfig() +
                '}';
    }
}
