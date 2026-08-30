package com.gokberkotlu.couriertrackingapp.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "courier-tracking.store")
public record StoreProperties(
    Resource storeFileResource, int radiusMeters, Duration reentryCooldown) {}
