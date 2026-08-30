package com.gokberkotlu.couriertrackingapp.properties;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "courier-tracking.courier")
public record CourierProperties(
    @PositiveOrZero double minMovementMeters, @Positive double maxSpeedKmh) {}
