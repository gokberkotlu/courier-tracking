package com.gokberkotlu.couriertrackingapp.store;

import com.gokberkotlu.couriertrackingapp.entity.StoreEntity;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.repository.StoreRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreCatalog {
  private final StoreRepository storeRepository;

  private volatile List<Store> stores = List.of();

  // Every ping is checked against every store, so the catalog is held in memory instead of
  // being read from the database on each one. Runs after StoreSeeder has written stores.json.
  @EventListener(ApplicationReadyEvent.class)
  @Order(2)
  public void load() {
    stores = storeRepository.findAll().stream().map(StoreCatalog::toStore).toList();
    log.info("Loaded {} stores into the catalog", stores.size());
  }

  public List<Store> stores() {
    return stores;
  }

  public Optional<Store> findById(Long storeId) {
    return stores.stream().filter(store -> store.id().equals(storeId)).findFirst();
  }

  private static Store toStore(StoreEntity entity) {
    return new Store(
        entity.getId(),
        entity.getName(),
        new GeoPoint(entity.getLat().doubleValue(), entity.getLng().doubleValue()));
  }
}
