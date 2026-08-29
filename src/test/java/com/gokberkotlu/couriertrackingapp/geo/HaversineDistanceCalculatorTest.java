package com.gokberkotlu.couriertrackingapp.geo;

import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.support.GeoPoints;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HaversineDistanceCalculator")
class HaversineDistanceCalculatorTest {
  private static final GeoPoint ATASEHIR_STORE = new GeoPoint(40.9923307, 29.1244229);
  private static final GeoPoint NOVADA_STORE = new GeoPoint(40.9861060, 29.1161293);

  private static final double MILLIMETER_TOLERANCE = 0.001;
  private static final double CENTIMETER_TOLERANCE = 0.01;

  private final DistanceCalculator calculator = new HaversineDistanceCalculator();

  @Test
  @DisplayName("given the same point twice, when distance is calculated, then it returns zero")
  void givenSamePoint_whenCalculatingDistance_thenReturnsZero() {
    double distance = calculator.distanceInMeters(ATASEHIR_STORE, ATASEHIR_STORE);
    Assertions.assertThat(distance).isZero();
  }

  @Test
  @DisplayName(
      "given two nearly identical points, when distance is calculated, then it stays a real number")
  void givenNearlyIdenticalPoints_whenCalculatingDistance_thenResultIsNotNaN() {
    GeoPoint almostIdentical = GeoPoints.atDistance(ATASEHIR_STORE, 0.001, GeoPoints.NORTH);
    double distance = calculator.distanceInMeters(ATASEHIR_STORE, almostIdentical);
    // A value that should mathematically be 1.0 can sometimes become 1.0000000000000002
    // due to floating-point precision, causing Math.asin(1.0000000000000002) to return NaN.
    Assertions.assertThat(distance)
        .as("floating point rounding must not push asin() out of its domain")
        .isNotNaN();
  }

  @Test
  @DisplayName("given two points, when arguments are swapped, then the distance is unchanged")
  void givenTwoPoints_whenArgumentsAreSwapped_thenDistanceIsUnchanged() {
    double forward = calculator.distanceInMeters(ATASEHIR_STORE, NOVADA_STORE);
    double backward = calculator.distanceInMeters(NOVADA_STORE, ATASEHIR_STORE);
    Assertions.assertThat(forward).isCloseTo(backward, Assertions.within(MILLIMETER_TOLERANCE));
  }

  @Test
  @DisplayName(
      "given a point generated 100 meters away, when distance is calculated, then it returns that distance")
  void givenPointGeneratedAtKnownDistance_whenCalculatingDistance_thenReturnsThatDistance() {
    double expectedMeters = 100;
    GeoPoint target = GeoPoints.atDistance(ATASEHIR_STORE, expectedMeters, GeoPoints.NORTH);
    double distance = calculator.distanceInMeters(ATASEHIR_STORE, target);
    Assertions.assertThat(distance)
        .isCloseTo(expectedMeters, Assertions.within(CENTIMETER_TOLERANCE));
  }
}
