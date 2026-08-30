package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.detection.EntranceDetector;
import com.gokberkotlu.couriertrackingapp.detection.LocationFilter;
import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.Store;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CourierLocationProcessor {
  private final LocationFilter locationFilter;
  private final TravelledDistanceCalculator travelledDistanceCalculator;
  private final EntranceDetector entranceDetector;

  public LocationIngestResult process(
      CourierState state, CourierLocation location, List<Store> stores) {

    if (locationFilter.isOutlier(state.getLastLocation(), location)) {
      return new LocationIngestResult(true, state.getTotalDistanceMeters(), List.of());
    }

    state.recordMovement(
        location, travelledDistanceCalculator.distanceTravelled(state.getLastLocation(), location));

    return new LocationIngestResult(
        false,
        state.getTotalDistanceMeters(),
        entranceDetector.detect(state.getCourierProximityState(), location, stores));
  }
}
