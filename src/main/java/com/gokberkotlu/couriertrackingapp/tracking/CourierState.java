package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.detection.CourierProximityState;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import lombok.Getter;

@Getter
public class CourierState {
  private final Long courierId;
  private final CourierProximityState courierProximityState = new CourierProximityState();

  private CourierLocation lastLocation;
  private double totalDistanceMeters;

  public CourierState(Long courierId) {
    this.courierId = courierId;
  }

  public static CourierState restoredFromSnapshot(
      Long courierId, CourierLocation lastLocation, double totalDistanceMeters) {
    CourierState state = new CourierState(courierId);
    state.lastLocation = lastLocation;
    state.totalDistanceMeters = totalDistanceMeters;
    return state;
  }

  public void recordMovement(CourierLocation location, double distanceMeters) {
    this.lastLocation = location;
    this.totalDistanceMeters += distanceMeters;
  }
}
