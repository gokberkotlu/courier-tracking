package com.gokberkotlu.couriertrackingapp.config;

import com.gokberkotlu.couriertrackingapp.detection.EntranceDetector;
import com.gokberkotlu.couriertrackingapp.detection.LocationFilter;
import com.gokberkotlu.couriertrackingapp.geo.DistanceCalculator;
import com.gokberkotlu.couriertrackingapp.geo.HaversineDistanceCalculator;
import com.gokberkotlu.couriertrackingapp.properties.CourierProperties;
import com.gokberkotlu.couriertrackingapp.properties.StoreProperties;
import com.gokberkotlu.couriertrackingapp.tracking.CourierStateRegistry;
import com.gokberkotlu.couriertrackingapp.tracking.TravelledDistanceCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the tracking domain. The domain classes carry no Spring annotations so that they stay plain
 * objects that can be unit tested without a container; their dependencies and tuning values are
 * supplied here instead.
 */
@Configuration
public class TrackingConfiguration {

  @Bean
  public DistanceCalculator distanceCalculator() {
    return new HaversineDistanceCalculator();
  }

  @Bean
  public EntranceDetector entranceDetector(
      DistanceCalculator distanceCalculator, StoreProperties storeProperties) {
    return new EntranceDetector(
        distanceCalculator, storeProperties.radiusMeters(), storeProperties.reentryCooldown());
  }

  @Bean
  public LocationFilter locationFilter(
      DistanceCalculator distanceCalculator, CourierProperties courierProperties) {
    return new LocationFilter(distanceCalculator, courierProperties.maxSpeedKmh());
  }

  @Bean
  public TravelledDistanceCalculator travelledDistanceCalculator(
      DistanceCalculator distanceCalculator, CourierProperties courierProperties) {
    return new TravelledDistanceCalculator(
        distanceCalculator, courierProperties.minMovementMeters());
  }

  @Bean
  public CourierStateRegistry courierStateRegistry() {
    return new CourierStateRegistry();
  }
}
