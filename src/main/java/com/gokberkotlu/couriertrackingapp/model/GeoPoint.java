package com.gokberkotlu.couriertrackingapp.model;

public record GeoPoint(double lat, double lng) {
  public GeoPoint {
    if (Double.isNaN(lat) || Double.isNaN(lng)) {
      throw new IllegalArgumentException("lat/lng must not be NaN");
    }
    if (lat < -90 || lat > 90) {
      throw new IllegalArgumentException("lat out of range: " + lat);
    }
    if (lng < -180 || lng > 180) {
      throw new IllegalArgumentException("lng out of range: " + lng);
    }
  }
}
