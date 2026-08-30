package com.gokberkotlu.couriertrackingapp.detection;

import com.gokberkotlu.couriertrackingapp.geo.HaversineDistanceCalculator;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import com.gokberkotlu.couriertrackingapp.support.CourierLocations;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntranceDetectorTest {
  private static final Long COURIER_ID = 1L;

  private static final Store ATASEHIR_STORE =
      new Store(1L, "Ataşehir MMM Migros", new GeoPoint(40.9923307, 29.1244229));
  private static final List<Store> STORES = List.of(ATASEHIR_STORE);

  private static final double RADIUS_METERS = 100;
  private static final Duration REENTRY_COOLDOWN = Duration.ofMinutes(1);

  private static final double INSIDE_RADIUS_METERS = 50;
  private static final double OUTSIDE_RADIUS_METERS = 500;

  private static final Instant FIRST_PING_AT = Instant.parse("2026-01-01T10:00:00Z");

  private EntranceDetector entranceDetector;
  private CourierProximityState courierProximityState;

  @BeforeEach
  void setUp() {
    entranceDetector =
        new EntranceDetector(new HaversineDistanceCalculator(), RADIUS_METERS, REENTRY_COOLDOWN);
    courierProximityState = new CourierProximityState();
  }

  @Test
  @DisplayName(
      "given a courier outside the radius, when it moves inside, then an entrance is recorded")
  void givenCourierOutsideRadius_whenItMovesInside_thenEntranceIsRecorded() {
    entranceDetector.detect(
        courierProximityState, locationAt(OUTSIDE_RADIUS_METERS, FIRST_PING_AT), STORES);

    Instant enteredAt = FIRST_PING_AT.plusSeconds(30);
    List<StoreEntrance> entrances =
        entranceDetector.detect(courierProximityState, locationAt(99, enteredAt), STORES);

    Assertions.assertThat(entrances)
        .singleElement()
        .satisfies(
            entrance -> {
              Assertions.assertThat(entrance.courierId()).isEqualTo(COURIER_ID);
              Assertions.assertThat(entrance.storeId()).isEqualTo(ATASEHIR_STORE.id());
              Assertions.assertThat(entrance.enteredAt()).isEqualTo(enteredAt);
            });
  }

  @Test
  @DisplayName(
      "given a courier exactly on the radius boundary, when detected, then it counts as inside")
  void givenCourierOnRadiusBoundary_whenDetected_thenItCountsAsInside() {
    List<StoreEntrance> entrances =
        entranceDetector.detect(
            courierProximityState, locationAt(RADIUS_METERS, FIRST_PING_AT), STORES);

    Assertions.assertThat(entrances)
        .as("the radius is inclusive: exactly %s meters is an entrance", RADIUS_METERS)
        .hasSize(1);
  }

  @Test
  @DisplayName("given a courier beyond the radius, when detected, then no entrance is recorded")
  void givenCourierBeyondRadius_whenDetected_thenNoEntranceIsRecorded() {
    List<StoreEntrance> entrances =
        entranceDetector.detect(courierProximityState, locationAt(101, FIRST_PING_AT), STORES);

    Assertions.assertThat(entrances).isEmpty();
  }

  @Test
  @DisplayName(
      "given a courier that stays inside the store, when it keeps sending pings, then only one entrance is recorded")
  void givenCourierStaysInsideStore_whenItKeepsSendingPings_thenOnlyOneEntranceIsRecorded() {
    Duration pingInterval = Duration.ofSeconds(10);
    int pingCount = 48; // eight minutes of dwelling

    List<StoreEntrance> entrances = new ArrayList<>();
    for (int i = 0; i < pingCount; i++) {
      Instant recordedAt = FIRST_PING_AT.plus(pingInterval.multipliedBy(i));
      entrances.addAll(
          entranceDetector.detect(
              courierProximityState, locationAt(INSIDE_RADIUS_METERS, recordedAt), STORES));
    }

    Assertions.assertThat(entrances)
        .as("staying inside is not a new entrance, only the transition into the radius is")
        .singleElement()
        .extracting(StoreEntrance::enteredAt)
        .isEqualTo(FIRST_PING_AT);
  }

  @Test
  @DisplayName(
      "given a courier that left the store, when it returns within the cooldown, then no new entrance is recorded")
  void givenCourierLeftStore_whenItReturnsWithinCooldown_thenNoNewEntranceIsRecorded() {
    entranceDetector.detect(
        courierProximityState, locationAt(INSIDE_RADIUS_METERS, FIRST_PING_AT), STORES);
    entranceDetector.detect(
        courierProximityState,
        locationAt(OUTSIDE_RADIUS_METERS, FIRST_PING_AT.plusSeconds(30)),
        STORES);

    List<StoreEntrance> entrances =
        entranceDetector.detect(
            courierProximityState,
            locationAt(INSIDE_RADIUS_METERS, FIRST_PING_AT.plusSeconds(59)),
            STORES);

    Assertions.assertThat(entrances)
        .as("re-entries within %s must be suppressed", REENTRY_COOLDOWN)
        .isEmpty();
  }

  @Test
  @DisplayName(
      "given a courier that left the store, when it returns after the cooldown, then a new entrance is recorded")
  void givenCourierLeftStore_whenItReturnsAfterCooldown_thenNewEntranceIsRecorded() {
    entranceDetector.detect(
        courierProximityState, locationAt(INSIDE_RADIUS_METERS, FIRST_PING_AT), STORES);
    entranceDetector.detect(
        courierProximityState,
        locationAt(OUTSIDE_RADIUS_METERS, FIRST_PING_AT.plusSeconds(30)),
        STORES);

    Instant returnedAt = FIRST_PING_AT.plusSeconds(61);
    List<StoreEntrance> entrances =
        entranceDetector.detect(
            courierProximityState, locationAt(INSIDE_RADIUS_METERS, returnedAt), STORES);

    Assertions.assertThat(entrances)
        .singleElement()
        .extracting(StoreEntrance::enteredAt)
        .isEqualTo(returnedAt);
  }

  private CourierLocation locationAt(double meters, Instant recordedAt) {
    return CourierLocations.nearStore(COURIER_ID, recordedAt, ATASEHIR_STORE, meters);
  }
}
