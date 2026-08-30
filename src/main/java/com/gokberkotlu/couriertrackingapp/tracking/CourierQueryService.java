package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.entity.CourierStateEntity;
import com.gokberkotlu.couriertrackingapp.entity.StoreEntranceEntity;
import com.gokberkotlu.couriertrackingapp.exception.CourierNotFoundException;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import com.gokberkotlu.couriertrackingapp.repository.CourierRepository;
import com.gokberkotlu.couriertrackingapp.repository.CourierStateRepository;
import com.gokberkotlu.couriertrackingapp.repository.StoreEntranceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourierQueryService {
  private final CourierRepository courierRepository;
  private final CourierStateRepository courierStateRepository;
  private final StoreEntranceRepository storeEntranceRepository;

  public Double getTotalTravelDistance(Long courierId) {
    requireCourier(courierId);
    return courierStateRepository
        .findById(courierId)
        .map(CourierStateEntity::getTotalDistanceMeters)
        .orElse(0.0);
  }

  public List<StoreEntrance> getStoreEntrances(Long courierId) {
    requireCourier(courierId);
    return storeEntranceRepository.findByCourierIdOrderByEnteredAtAsc(courierId).stream()
        .map(CourierQueryService::toStoreEntrance)
        .toList();
  }

  private void requireCourier(Long courierId) {
    if (!courierRepository.existsById(courierId)) {
      throw new CourierNotFoundException(courierId);
    }
  }

  private static StoreEntrance toStoreEntrance(StoreEntranceEntity entity) {
    return new StoreEntrance(
        entity.getCourierId(),
        entity.getStoreId(),
        entity.getEnteredAt(),
        new GeoPoint(entity.getLat().doubleValue(), entity.getLng().doubleValue()),
        entity.getDistanceToStoreMeters());
  }
}
