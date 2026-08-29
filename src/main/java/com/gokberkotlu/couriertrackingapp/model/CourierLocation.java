package com.gokberkotlu.couriertrackingapp.model;

import java.time.Instant;
import java.util.Objects;

public record CourierLocation(Long courierId, GeoPoint geoPoint, Instant recordedAt) {
  public CourierLocation {
    Objects.requireNonNull(courierId, "'courierId' must not be null");
    Objects.requireNonNull(geoPoint, "'geoPoint' must not be null");
    Objects.requireNonNull(recordedAt, "'recordedAt' must not be null");
  }
}
