package com.gokberkotlu.couriertrackingapp.detection;

import com.gokberkotlu.couriertrackingapp.geo.HaversineDistanceCalculator;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.support.GeoPoints;
import java.time.Duration;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocationFilter")
class LocationFilterTest {
  private static final Long COURIER_ID = 1L;
  private static final GeoPoint ORIGIN = new GeoPoint(40.9923307, 29.1244229);
  private static final Instant FIRST_PING_AT = Instant.parse("2026-01-01T10:00:00Z");
  private static final double MAX_SPEED_KMH = 150;

  private final LocationFilter locationFilter =
      new LocationFilter(new HaversineDistanceCalculator(), MAX_SPEED_KMH);

  @Test
  @DisplayName(
      "given two pings less than a second apart, when the speed is plausible, then the ping is kept")
  void givenPingsLessThanASecondApart_whenSpeedIsPlausible_thenPingIsKept() {
    // 5 metres in 400 ms is 45 km/h. Measuring the gap in whole seconds would round it to zero
    // and make the speed infinite, which would discard every fast-arriving ping.
    CourierLocation previous = locationAt(0, FIRST_PING_AT);
    CourierLocation current = locationAt(5, FIRST_PING_AT.plusMillis(400));

    Assertions.assertThat(locationFilter.isOutlier(previous, current)).isFalse();
  }

  @Test
  @DisplayName(
      "given a ping that is not newer than the last one, when filtered, then it is dropped")
  void givenPingThatIsNotNewerThanTheLastOne_whenFiltered_thenItIsDropped() {
    CourierLocation previous = locationAt(0, FIRST_PING_AT.plus(Duration.ofMinutes(1)));

    Assertions.assertThat(locationFilter.isOutlier(previous, locationAt(5, FIRST_PING_AT)))
        .as("pings that arrive out of order carry no usable movement")
        .isTrue();
    Assertions.assertThat(locationFilter.isOutlier(previous, locationAt(5, previous.recordedAt())))
        .as("two pings with the same timestamp carry no elapsed time to measure against")
        .isTrue();
  }

  private CourierLocation locationAt(double meters, Instant recordedAt) {
    return new CourierLocation(
        COURIER_ID, GeoPoints.atDistance(ORIGIN, meters, GeoPoints.NORTH), recordedAt);
  }
}
