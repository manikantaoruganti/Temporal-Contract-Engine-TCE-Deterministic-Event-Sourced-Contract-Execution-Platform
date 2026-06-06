package com.tce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TemporalContractEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemporalContractEngineApplication.class, args);
    }

}
