package com.gokberkotlu.couriertrackingapp.support;

import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourierLocations {
  /** A location for the given courier, at the given distance and bearing from a store. */
  public static CourierLocation nearStore(
      Long courierId, Instant recordedAt, Store store, double meters) {
    GeoPoint geoPoint = GeoPoints.atDistance(store.geoPoint(), meters, GeoPoints.NORTH);
    return new CourierLocation(courierId, geoPoint, recordedAt);
  }
}
