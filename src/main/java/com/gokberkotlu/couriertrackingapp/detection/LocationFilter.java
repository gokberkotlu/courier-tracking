package com.gokberkotlu.couriertrackingapp.detection;

import com.gokberkotlu.couriertrackingapp.geo.DistanceCalculator;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import java.time.Duration;

/** Decides whether a location ping can be trusted. */
public class LocationFilter {
  private final DistanceCalculator distanceCalculator;
  private final double maxSpeedMetersPerSecond;

  public LocationFilter(DistanceCalculator distanceCalculator, double maxSpeedKmh) {
    this.distanceCalculator = distanceCalculator;
    // 1/3.6 -> 1000m/3600s
    this.maxSpeedMetersPerSecond = maxSpeedKmh / 3.6;
  }

  public boolean isOutlier(CourierLocation previous, CourierLocation current) {
    if (previous == null) {
      return false;
    }

    Duration elapsed = Duration.between(previous.recordedAt(), current.recordedAt());
    if (elapsed.isZero() || elapsed.isNegative()) {
      return true;
    }

    double meters = distanceCalculator.distanceInMeters(previous.geoPoint(), current.geoPoint());
    // toSeconds() rounds down, so gaps under one second would become zero here
    double elapsedSeconds = elapsed.toMillis() / 1000.0;
    double speedMetersPerSecond = meters / elapsedSeconds;

    return speedMetersPerSecond > maxSpeedMetersPerSecond;
  }
}
