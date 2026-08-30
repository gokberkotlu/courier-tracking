package com.gokberkotlu.couriertrackingapp.detection;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tracks, for a single courier, whether it is currently inside each store's radius and when it last
 * entered that store.
 */
public class CourierProximityState {
  private final Map<Long, Boolean> insideByStoreId = new HashMap<>();
  private final Map<Long, Instant> lastEntranceByStoreId = new HashMap<>();

  public boolean isInsideStore(Long storeId) {
    return insideByStoreId.getOrDefault(storeId, false);
  }

  public void setInsideStore(Long storeId, boolean inside) {
    insideByStoreId.put(storeId, inside);
  }

  public Optional<Instant> lastEntranceAtStore(Long storeId) {
    return Optional.ofNullable(lastEntranceByStoreId.get(storeId));
  }

  public void recordLastEntranceAtStore(Long storeId, Instant lastEntrance) {
    lastEntranceByStoreId.put(storeId, lastEntrance);
  }
}
