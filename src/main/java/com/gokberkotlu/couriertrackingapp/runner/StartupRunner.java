package com.gokberkotlu.couriertrackingapp.runner;

import com.gokberkotlu.couriertrackingapp.properties.CourierProperties;
import com.gokberkotlu.couriertrackingapp.properties.StoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartupRunner implements CommandLineRunner {
  private final StoreProperties storeProperties;
  private final CourierProperties courierProperties;

  @Override
  public void run(String... args) throws Exception {
    System.out.println(1);
  }
}
