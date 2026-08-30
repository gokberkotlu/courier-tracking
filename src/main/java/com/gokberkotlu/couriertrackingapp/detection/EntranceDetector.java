package com.gokberkotlu.couriertrackingapp.detection;

import com.gokberkotlu.couriertrackingapp.geo.DistanceCalculator;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

/**
 * Detects when a courier enters a store's radius.
 *
 * <p>An entrance is an OUTSIDE to INSIDE transition, so a courier that stays within the radius
 * produces a single entrance no matter how many pings it sends. A cooldown additionally suppresses
 * repeated entrances caused by GPS jitter at the boundary.
 */
@AllArgsConstructor
public class EntranceDetector {
  private final DistanceCalculator distanceCalculator;
  private final double radiusMeters;
  private final Duration reentryCooldown;

  // Tolerance for the radius comparison
  private static final double DISTANCE_EPSILON_METERS = 0.001;

  public List<StoreEntrance> detect(
      CourierProximityState courierProximityState,
      CourierLocation courierLocation,
      List<Store> stores) {
    List<StoreEntrance> entrances = new ArrayList<>();

    for (Store store : stores) {
      double distance =
          distanceCalculator.distanceInMeters(courierLocation.geoPoint(), store.geoPoint());
      boolean inside = isInside(distance);
      boolean wasInside = courierProximityState.isInsideStore(store.id());

      courierProximityState.setInsideStore(store.id(), inside);

      if (!inside || wasInside) {
        continue;
      }
      if (isWithinCooldown(courierProximityState, store.id(), courierLocation.recordedAt())) {
        continue;
      }

      courierProximityState.recordLastEntranceAtStore(store.id(), courierLocation.recordedAt());
      entrances.add(
          new StoreEntrance(
              courierLocation.courierId(),
              store.id(),
              courierLocation.recordedAt(),
              courierLocation.geoPoint(),
              distance));
    }

    return entrances;
  }

  public boolean isWithinRadius(GeoPoint point, Store store) {
    return isInside(distanceCalculator.distanceInMeters(point, store.geoPoint()));
  }

  private boolean isInside(double distanceMeters) {
    return distanceMeters <= radiusMeters + DISTANCE_EPSILON_METERS;
  }

  private boolean isWithinCooldown(CourierProximityState state, Long storeId, Instant at) {
    return state
        .lastEntranceAtStore(storeId)
        .map(last -> Duration.between(last, at).compareTo(reentryCooldown) < 0)
        .orElse(false);
  }
}
