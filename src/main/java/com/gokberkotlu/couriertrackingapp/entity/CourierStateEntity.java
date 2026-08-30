package com.gokberkotlu.couriertrackingapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "courier_state")
public class CourierStateEntity {
  @Id private Long courierId;

  @Column(precision = 9, scale = 6)
  private BigDecimal lastLat;

  @Column(precision = 9, scale = 6)
  private BigDecimal lastLng;

  private Instant lastRecordedAt;

  @Column(nullable = false)
  private double totalDistanceMeters;

  @Version private Long version;

  public CourierStateEntity(Long courierId) {
    this.courierId = courierId;
  }

  public void update(
      BigDecimal lat, BigDecimal lng, Instant recordedAt, double totalDistanceMeters) {
    this.lastLat = lat;
    this.lastLng = lng;
    this.lastRecordedAt = recordedAt;
    this.totalDistanceMeters = totalDistanceMeters;
  }
}
