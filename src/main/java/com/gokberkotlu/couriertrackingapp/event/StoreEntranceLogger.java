package com.gokberkotlu.couriertrackingapp.event;

import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import com.gokberkotlu.couriertrackingapp.store.StoreCatalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreEntranceLogger {
  private final StoreCatalog storeCatalog;

  @EventListener
  public void onStoreEntrance(StoreEntranceDetectedEvent event) {
    StoreEntrance entrance = event.storeEntrance();
    String storeName =
        storeCatalog
            .findById(entrance.storeId())
            .map(Store::name)
            .orElseGet(() -> "store " + entrance.storeId());

    log.info(
        "Courier {} entered {} at {} ({} metres from the store)",
        entrance.courierId(),
        storeName,
        entrance.enteredAt(),
        Math.round(entrance.distanceToStoreMeters()));
  }
}
