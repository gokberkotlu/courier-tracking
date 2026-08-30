package com.gokberkotlu.couriertrackingapp.store;

import com.gokberkotlu.couriertrackingapp.entity.StoreEntity;
import com.gokberkotlu.couriertrackingapp.exception.StoreDataLoadException;
import com.gokberkotlu.couriertrackingapp.repository.StoreRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreSeeder {
  private final StoreJsonReader storeJsonReader;
  private final StoreRepository storeRepository;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seedStore() {
    List<StoreJsonModel> stores = storeJsonReader.read();
    if (stores.isEmpty()) {
      throw new StoreDataLoadException("Store data file contains no stores");
    }

    Map<String, StoreEntity> existingByName =
        storeRepository.findAll().stream()
            .collect(Collectors.toMap(StoreEntity::getName, Function.identity()));

    int created = 0;
    int updated = 0;

    for (StoreJsonModel store : stores) {
      StoreEntity existing = existingByName.get(store.name());
      if (existing == null) {
        storeRepository.save(new StoreEntity(store.name(), store.lat(), store.lng()));
        created++;
      } else {
        existing.updateCoordinates(store.lat(), store.lng());
        updated++;
      }
    }

    log.info("Created stores: {}, updated: {}", created, updated);
  }
}
