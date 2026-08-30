package com.gokberkotlu.couriertrackingapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store_entrance")
public class StoreEntranceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long courierId;

  @Column(nullable = false)
  private Long storeId;

  @Column(nullable = false)
  private Instant enteredAt;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lat;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lng;

  @Column(nullable = false)
  private double distanceToStoreMeters;

  public StoreEntranceEntity(
      Long courierId,
      Long storeId,
      Instant enteredAt,
      BigDecimal lat,
      BigDecimal lng,
      double distanceToStoreMeters) {
    this.courierId = courierId;
    this.storeId = storeId;
    this.enteredAt = enteredAt;
    this.lat = lat;
    this.lng = lng;
    this.distanceToStoreMeters = distanceToStoreMeters;
  }
}
