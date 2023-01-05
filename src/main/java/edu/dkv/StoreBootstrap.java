package edu.dkv;

import edu.dkv.app.GossipApplication;
import edu.dkv.app.ProcessBasedApplication;
import edu.dkv.sdk.KVStoreApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"edu.dkv"})
public class StoreBootstrap {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(StoreBootstrap.class, args);
        KVStoreApplication storeApplication = context.getBean(GossipApplication.class);
        storeApplication.run();
    }
}
