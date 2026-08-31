package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CourierStateRegistry")
class CourierStateRegistryTest {
  private static final Long COURIER_ID = 1L;
  private static final CourierLocation ANY_LOCATION =
      new CourierLocation(
          COURIER_ID, new GeoPoint(40.9923307, 29.1244229), Instant.parse("2026-01-01T10:00:00Z"));

  private static final int THREADS = 16;
  private static final int PINGS_PER_THREAD = 250;
  private static final double METERS_PER_PING = 1;

  private final CourierStateRegistry registry = new CourierStateRegistry();

  @Test
  @DisplayName(
      "given pings for one courier arriving in parallel, when they are applied, then no distance is lost")
  void givenParallelPingsForOneCourier_whenApplied_thenNoDistanceIsLost() throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> pings = new ArrayList<>();

    try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
      for (int thread = 0; thread < THREADS; thread++) {
        pings.add(
            executor.submit(
                () -> {
                  start.await();
                  for (int ping = 0; ping < PINGS_PER_THREAD; ping++) {
                    registry.update(COURIER_ID, CourierState::new, this::record);
                  }
                  return null;
                }));
      }
      start.countDown();

      // Workers hand their failures to the Future rather than to the test thread, so a worker
      // that dies would otherwise show up only as a puzzling shortfall in the total.
      for (Future<?> ping : pings) {
        ping.get();
      }
    }

    Assertions.assertThat(totalDistance())
        .as("every update must be applied exactly once, with none overwriting another")
        .isEqualTo(THREADS * PINGS_PER_THREAD * METERS_PER_PING);
  }

  @Test
  @DisplayName(
      "given an update that fails, when the next ping arrives, then the state is loaded again")
  void givenUpdateThatFails_whenNextPingArrives_thenStateIsLoadedAgain() {
    AtomicInteger loads = new AtomicInteger();
    Function<Long, CourierState> loader =
        courierId -> {
          loads.incrementAndGet();
          return new CourierState(courierId);
        };

    registry.update(COURIER_ID, loader, this::record);
    registry.update(COURIER_ID, loader, this::record);
    Assertions.assertThat(loads).as("a courier already held in memory is not reloaded").hasValue(1);

    Assertions.assertThatThrownBy(
            () ->
                registry.update(
                    COURIER_ID,
                    loader,
                    state -> {
                      state.recordMovement(ANY_LOCATION, METERS_PER_PING);
                      throw new IllegalStateException("persistence failed");
                    }))
        .isInstanceOf(IllegalStateException.class);

    Assertions.assertThat(registry.update(COURIER_ID, loader, this::record))
        .as("the half-applied state was dropped, so the next ping starts from what was persisted")
        .isEqualTo(METERS_PER_PING);
    Assertions.assertThat(loads).hasValue(2);
  }

  private Double record(CourierState state) {
    state.recordMovement(ANY_LOCATION, METERS_PER_PING);
    return state.getTotalDistanceMeters();
  }

  private double totalDistance() {
    return registry.update(COURIER_ID, CourierState::new, CourierState::getTotalDistanceMeters);
  }
}
