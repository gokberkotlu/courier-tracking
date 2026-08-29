package com.gokberkotlu.couriertrackingapp.geo;

import com.gokberkotlu.couriertrackingapp.model.GeoPoint;

public class HaversineDistanceCalculator implements DistanceCalculator {
  private static final double EARTH_RADIUS_METERS = 6_371_008.8;

  @Override
  public double distanceInMeters(GeoPoint from, GeoPoint to) {
    double lat1 = Math.toRadians(from.lat());
    double lat2 = Math.toRadians(to.lat());
    double deltaLat = lat2 - lat1;
    double deltaLng = Math.toRadians(to.lng() - from.lng());

    double a =
        Math.pow(Math.sin(deltaLat / 2), 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLng / 2), 2);

    return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1.0, Math.sqrt(a)));
  }
}
