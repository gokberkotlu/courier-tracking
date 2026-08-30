package com.gokberkotlu.couriertrackingapp.detection;

import com.gokberkotlu.couriertrackingapp.geo.DistanceCalculator;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CourierTravelledDistanceCalculator {
  private final DistanceCalculator distanceCalculator;
  private final double minMovementMeters;

  public double distanceTravelled(
      CourierLocation previousLocation, CourierLocation currentLocation) {
    if (previousLocation == null) {
      return 0;
    }

    double meters =
        distanceCalculator.distanceInMeters(
            previousLocation.geoPoint(), currentLocation.geoPoint());

    return meters < minMovementMeters ? 0 : meters;
  }
}
