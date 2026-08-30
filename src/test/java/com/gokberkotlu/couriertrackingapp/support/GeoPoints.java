package com.gokberkotlu.couriertrackingapp.support;

import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeoPoints {
  private static final double EARTH_RADIUS_METERS = 6_371_008.8;

  public static final double NORTH = 0.0;
  public static final double EAST = 90.0;
  public static final double SOUTH = 180.0;
  public static final double WEST = 270.0;

  public static GeoPoint atDistance(GeoPoint origin, double meters, double bearingDegrees) {
    double angularDistance = meters / EARTH_RADIUS_METERS;
    double bearing = Math.toRadians(bearingDegrees);
    double lat1 = Math.toRadians(origin.lat());
    double lng1 = Math.toRadians(origin.lng());

    double lat2 =
        Math.asin(
            Math.sin(lat1) * Math.cos(angularDistance)
                + Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearing));

    double lng2 =
        lng1
            + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

    return new GeoPoint(Math.toDegrees(lat2), (Math.toDegrees(lng2) + 540) % 360 - 180);
  }
}
