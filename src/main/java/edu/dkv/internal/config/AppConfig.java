package edu.dkv.internal.config;

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

    private int maxRunningTimeInMinutes;

    public long getRunningTime() {
        return (long) maxRunningTimeInMinutes * 60 * 1000;
    }
}
