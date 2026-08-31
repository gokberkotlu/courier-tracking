package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.entity.CourierLocationEntity;
import com.gokberkotlu.couriertrackingapp.entity.CourierStateEntity;
import com.gokberkotlu.couriertrackingapp.entity.StoreEntranceEntity;
import com.gokberkotlu.couriertrackingapp.event.StoreEntranceDetectedEvent;
import com.gokberkotlu.couriertrackingapp.exception.CourierNotFoundException;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import com.gokberkotlu.couriertrackingapp.repository.CourierLocationRepository;
import com.gokberkotlu.couriertrackingapp.repository.CourierRepository;
import com.gokberkotlu.couriertrackingapp.repository.CourierStateRepository;
import com.gokberkotlu.couriertrackingapp.repository.StoreEntranceRepository;
import com.gokberkotlu.couriertrackingapp.store.StoreCatalog;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourierLocationIngestService {
  private static final int COORDINATE_SCALE = 6;

  private final CourierStateRegistry courierStateRegistry;
  private final CourierStateLoader courierStateLoader;
  private final CourierLocationProcessor courierLocationProcessor;
  private final StoreCatalog storeCatalog;
  private final CourierRepository courierRepository;
  private final CourierLocationRepository courierLocationRepository;
  private final CourierStateRepository courierStateRepository;
  private final StoreEntranceRepository storeEntranceRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public LocationIngestResult ingest(CourierLocation location) {
    if (!courierRepository.existsById(location.courierId())) {
      throw new CourierNotFoundException(location.courierId());
    }

    // Pings for one courier are applied one at a time so that the distance accumulated in
    // memory and the entrance transitions cannot interleave.
    return courierStateRegistry.update(
        location.courierId(),
        courierStateLoader::load,
        state -> {
          LocationIngestResult result =
              courierLocationProcessor.process(state, location, storeCatalog.stores());
          persist(state, location, result);
          return result;
        });
  }

  private void persist(CourierState state, CourierLocation location, LocationIngestResult result) {
    courierLocationRepository.save(
        new CourierLocationEntity(
            location.courierId(),
            location.recordedAt(),
            coordinate(location.geoPoint().lat()),
            coordinate(location.geoPoint().lng()),
            result.outlier()));

    if (result.outlier()) {
      return;
    }

    saveSnapshot(state);

    for (StoreEntrance entrance : result.storeEntrances()) {
      saveEntrance(entrance);
      eventPublisher.publishEvent(new StoreEntranceDetectedEvent(entrance));
    }
  }

  private void saveSnapshot(CourierState state) {
    CourierStateEntity entity =
        courierStateRepository
            .findById(state.getCourierId())
            .orElseGet(() -> new CourierStateEntity(state.getCourierId()));

    CourierLocation last = state.getLastLocation();
    entity.update(
        coordinate(last.geoPoint().lat()),
        coordinate(last.geoPoint().lng()),
        last.recordedAt(),
        state.getTotalDistanceMeters());

    courierStateRepository.save(entity);
  }

  private void saveEntrance(StoreEntrance entrance) {
    storeEntranceRepository.save(
        new StoreEntranceEntity(
            entrance.courierId(),
            entrance.storeId(),
            entrance.enteredAt(),
            coordinate(entrance.geoPoint().lat()),
            coordinate(entrance.geoPoint().lng()),
            entrance.distanceToStoreMeters()));
  }

  private static BigDecimal coordinate(double value) {
    return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
  }
}
