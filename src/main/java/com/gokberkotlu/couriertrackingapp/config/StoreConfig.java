package com.gokberkotlu.couriertrackingapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "courier-tracking.store")
public class StoreConfig {
  // radius-meters
  private int radiusMeters;
  // reentry-cooldown
  private String reentryCooldown;
}
