package com.gokberkotlu.couriertrackingapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store")
public class StoreEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 128)
  private String name;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lat;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal lng;

  public StoreEntity(String name, BigDecimal lat, BigDecimal lng) {
    this.name = name;
    this.lat = lat;
    this.lng = lng;
  }

  public void updateCoordinates(BigDecimal lat, BigDecimal lng) {
    this.lat = lat;
    this.lng = lng;
  }
}
