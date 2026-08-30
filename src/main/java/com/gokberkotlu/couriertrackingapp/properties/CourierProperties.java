package com.gokberkotlu.couriertrackingapp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "courier-tracking.courier")
public record CourierProperties(double minMovementMeters, double maxSpeedKmh) {}
