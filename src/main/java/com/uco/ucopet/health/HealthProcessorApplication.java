package com.uco.ucopet.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HealthProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthProcessorApplication.class, args);
    }
}
