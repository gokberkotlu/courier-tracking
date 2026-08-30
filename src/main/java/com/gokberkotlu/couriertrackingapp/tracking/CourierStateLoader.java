package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.detection.CourierProximityState;
import com.gokberkotlu.couriertrackingapp.detection.EntranceDetector;
import com.gokberkotlu.couriertrackingapp.entity.CourierStateEntity;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.repository.CourierStateRepository;
import com.gokberkotlu.couriertrackingapp.repository.StoreEntranceRepository;
import com.gokberkotlu.couriertrackingapp.store.StoreCatalog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CourierStateLoader {
  private final CourierStateRepository courierStateRepository;
  private final StoreEntranceRepository storeEntranceRepository;
  private final StoreCatalog storeCatalog;
  private final EntranceDetector entranceDetector;

  public CourierState load(Long courierId) {
    Optional<CourierStateEntity> snapshot = courierStateRepository.findById(courierId);

    CourierLocation lastLocation =
        snapshot
            .filter(state -> state.getLastRecordedAt() != null)
            .map(CourierStateLoader::toLocation)
            .orElse(null);

    return CourierState.restoredFromSnapshot(
        courierId,
        restoreProximity(courierId, lastLocation),
        lastLocation,
        snapshot.map(CourierStateEntity::getTotalDistanceMeters).orElse(0.0));
  }

  private CourierProximityState restoreProximity(Long courierId, CourierLocation lastLocation) {
    CourierProximityState proximityState = new CourierProximityState();

    storeEntranceRepository
        .findLastEntrancePerStore(courierId)
        .forEach(
            entrance ->
                proximityState.recordLastEntranceAtStore(entrance.storeId(), entrance.enteredAt()));

    if (lastLocation != null) {
      // Without this, a courier that was standing inside a store when the application stopped
      // would look like a fresh outside-to-inside transition on its next ping.
      for (Store store : storeCatalog.stores()) {
        proximityState.setInsideStore(
            store.id(), entranceDetector.isWithinRadius(lastLocation.geoPoint(), store));
      }
    }

    return proximityState;
  }

  private static CourierLocation toLocation(CourierStateEntity state) {
    return new CourierLocation(
        state.getCourierId(),
        new GeoPoint(state.getLastLat().doubleValue(), state.getLastLng().doubleValue()),
        state.getLastRecordedAt());
  }
}
