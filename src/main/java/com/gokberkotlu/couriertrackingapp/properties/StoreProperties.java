package com.gokberkotlu.couriertrackingapp.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "courier-tracking.store")
public record StoreProperties(
    @NotNull Resource storeFileResource,
    @Positive int radiusMeters,
    @NotNull Duration reentryCooldown) {}
