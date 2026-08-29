package com.gokberkotlu.couriertrackingapp.runner;

import com.gokberkotlu.couriertrackingapp.config.StoreConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartupRunner implements CommandLineRunner {
  private final StoreConfig storeConfig;

  @Override
  public void run(String... args) throws Exception {
    System.out.println(storeConfig.toString());
  }
}
