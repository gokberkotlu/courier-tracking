package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.store.StoreCatalog;
import com.gokberkotlu.couriertrackingapp.support.GeoPoints;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CourierTrackingFlowTest {
  private static final Instant START = Instant.parse("2026-01-01T10:00:00Z");
  private static final double METER_TOLERANCE = 1.0;

  // Each test uses its own courier so that the shared in-memory registry and database
  // cannot leak state between them.
  private static final Long ROUTE_COURIER = 1L;
  private static final Long RESTART_COURIER = 2L;
  private static final Long OUTLIER_COURIER = 3L;

  @Autowired private CourierLocationIngestService ingestService;
  @Autowired private CourierQueryService queryService;
  @Autowired private CourierStateLoader courierStateLoader;
  @Autowired private StoreCatalog storeCatalog;

  @Test
  @DisplayName(
      "given a route that passes through a store, when the pings are ingested, then one entrance is logged and the distance accumulates")
  void givenRouteThroughStore_whenIngested_thenEntranceIsLoggedAndDistanceAccumulates() {
    Store store = anyStore();

    ingest(ROUTE_COURIER, store, 500, START);
    ingest(ROUTE_COURIER, store, 50, START.plusSeconds(120));
    ingest(ROUTE_COURIER, store, 50, START.plusSeconds(180));
    ingest(ROUTE_COURIER, store, 500, START.plusSeconds(300));

    Assertions.assertThat(queryService.getStoreEntrances(ROUTE_COURIER))
        .singleElement()
        .satisfies(
            entrance -> {
              Assertions.assertThat(entrance.storeId()).isEqualTo(store.id());
              Assertions.assertThat(entrance.enteredAt()).isEqualTo(START.plusSeconds(120));
            });

    Assertions.assertThat(queryService.getTotalTravelDistance(ROUTE_COURIER))
        .as("450 metres in, no movement while dwelling, 450 metres out")
        .isCloseTo(900.0, Assertions.within(METER_TOLERANCE));
  }

  @Test
  @DisplayName(
      "given a courier inside a store, when its state is reloaded from the database, then it is still marked as inside")
  void givenCourierInsideStore_whenStateIsReloaded_thenItIsStillMarkedInside() {
    Store store = anyStore();

    ingest(RESTART_COURIER, store, 500, START);
    ingest(RESTART_COURIER, store, 50, START.plusSeconds(120));

    CourierState reloaded = courierStateLoader.load(RESTART_COURIER);

    Assertions.assertThat(reloaded.getCourierProximityState().isInsideStore(store.id()))
        .as("a restart must not turn dwelling inside the radius into a new entrance")
        .isTrue();
    Assertions.assertThat(reloaded.getCourierProximityState().lastEntranceAtStore(store.id()))
        .contains(START.plusSeconds(120));
  }

  @Test
  @DisplayName(
      "given a ping that implies an impossible speed, when it is ingested, then it is rejected and the distance is unchanged")
  void givenImpossiblySpeedyPing_whenIngested_thenItIsRejectedAndDistanceIsUnchanged() {
    Store store = anyStore();

    ingest(OUTLIER_COURIER, store, 0, START);
    LocationIngestResult result = ingest(OUTLIER_COURIER, store, 100_000, START.plusSeconds(10));

    Assertions.assertThat(result.outlier()).isTrue();
    Assertions.assertThat(queryService.getTotalTravelDistance(OUTLIER_COURIER)).isZero();
  }

  private LocationIngestResult ingest(Long courierId, Store store, double meters, Instant at) {
    GeoPoint point = GeoPoints.atDistance(store.geoPoint(), meters, GeoPoints.NORTH);
    return ingestService.ingest(new CourierLocation(courierId, point, at));
  }

  private Store anyStore() {
    return storeCatalog.stores().getFirst();
  }
}
