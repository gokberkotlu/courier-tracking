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
@Table(name = "courier_location")
public class CourierLocationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long courierId;

  @Column(nullable = false)
  private Instant recordedAt;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lat;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lng;

  @Column(nullable = false)
  private boolean outlier;

  public CourierLocationEntity(
      Long courierId, Instant recordedAt, BigDecimal lat, BigDecimal lng, boolean outlier) {
    this.courierId = courierId;
    this.recordedAt = recordedAt;
    this.lat = lat;
    this.lng = lng;
    this.outlier = outlier;
  }
}
