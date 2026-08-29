package com.gokberkotlu.couriertrackingapp.geo;

import com.gokberkotlu.couriertrackingapp.model.GeoPoint;

public interface DistanceCalculator {
  double distanceInMeters(GeoPoint from, GeoPoint to);
}
