package com.example.vectordb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VectorDbDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VectorDbDemoApplication.class, args);
    }
}
