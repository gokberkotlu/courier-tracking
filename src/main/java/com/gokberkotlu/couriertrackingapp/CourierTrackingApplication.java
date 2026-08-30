package com.gokberkotlu.couriertrackingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class CourierTrackingApplication {

  public static void main(String[] args) {
    SpringApplication.run(CourierTrackingApplication.class, args);
  }
}
