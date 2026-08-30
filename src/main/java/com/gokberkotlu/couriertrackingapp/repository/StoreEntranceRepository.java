package com.gokberkotlu.couriertrackingapp.repository;

import com.gokberkotlu.couriertrackingapp.entity.StoreEntranceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreEntranceRepository extends JpaRepository<StoreEntranceEntity, Long> {

  List<StoreEntranceEntity> findByCourierIdOrderByEnteredAtAsc(Long courierId);

  @Query(
      """
      select new com.gokberkotlu.couriertrackingapp.repository.LastStoreEntrance(
          e.storeId, max(e.enteredAt))
      from StoreEntranceEntity e
      where e.courierId = :courierId
      group by e.storeId
      """)
  List<LastStoreEntrance> findLastEntrancePerStore(Long courierId);
}
