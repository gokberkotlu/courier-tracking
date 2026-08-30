package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.detection.CourierProximityState;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import lombok.Getter;

@Getter
public class CourierState {
  private final Long courierId;
  private final CourierProximityState courierProximityState;

  private CourierLocation lastLocation;
  private double totalDistanceMeters;

  private CourierState(
      Long courierId,
      CourierProximityState courierProximityState,
      CourierLocation lastLocation,
      double totalDistanceMeters) {
    this.courierId = courierId;
    this.courierProximityState = courierProximityState;
    this.lastLocation = lastLocation;
    this.totalDistanceMeters = totalDistanceMeters;
  }

  public CourierState(Long courierId) {
    this(courierId, new CourierProximityState(), null, 0);
  }

  public static CourierState restoredFromSnapshot(
      Long courierId,
      CourierProximityState courierProximityState,
      CourierLocation lastLocation,
      double totalDistanceMeters) {
    return new CourierState(courierId, courierProximityState, lastLocation, totalDistanceMeters);
  }

  public void recordMovement(CourierLocation location, double distanceMeters) {
    this.lastLocation = location;
    this.totalDistanceMeters += distanceMeters;
  }
}
