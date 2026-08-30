package com.gokberkotlu.couriertrackingapp.web;

import com.gokberkotlu.couriertrackingapp.model.CourierLocation;
import com.gokberkotlu.couriertrackingapp.model.GeoPoint;
import com.gokberkotlu.couriertrackingapp.model.Store;
import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import com.gokberkotlu.couriertrackingapp.store.StoreCatalog;
import com.gokberkotlu.couriertrackingapp.tracking.CourierLocationIngestService;
import com.gokberkotlu.couriertrackingapp.tracking.CourierQueryService;
import com.gokberkotlu.couriertrackingapp.tracking.LocationIngestResult;
import com.gokberkotlu.couriertrackingapp.web.dto.CourierLocationRequest;
import com.gokberkotlu.couriertrackingapp.web.dto.LocationIngestResponse;
import com.gokberkotlu.couriertrackingapp.web.dto.StoreEntranceResponse;
import com.gokberkotlu.couriertrackingapp.web.dto.TotalDistanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Couriers", description = "Location ingestion and courier queries")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/couriers/{courierId}")
public class CourierController {
  private final CourierLocationIngestService courierLocationIngestService;
  private final CourierQueryService courierQueryService;
  private final StoreCatalog storeCatalog;

  @Operation(summary = "Record a courier location and report any store entrance it triggered")
  @PostMapping("/locations")
  public LocationIngestResponse recordLocation(
      @PathVariable Long courierId, @Valid @RequestBody CourierLocationRequest request) {

    LocationIngestResult result =
        courierLocationIngestService.ingest(
            new CourierLocation(
                courierId, new GeoPoint(request.lat(), request.lng()), request.recordedAt()));

    return new LocationIngestResponse(
        !result.outlier(), result.totalDistanceMeters(), toResponses(result.storeEntrances()));
  }

  @Operation(summary = "Total distance the courier has travelled, in metres")
  @GetMapping("/total-distance")
  public TotalDistanceResponse getTotalTravelDistance(@PathVariable Long courierId) {
    return new TotalDistanceResponse(
        courierId, courierQueryService.getTotalTravelDistance(courierId));
  }

  @Operation(summary = "Store entrances logged for the courier, oldest first")
  @GetMapping("/store-entrances")
  public List<StoreEntranceResponse> getStoreEntrances(@PathVariable Long courierId) {
    return toResponses(courierQueryService.getStoreEntrances(courierId));
  }

  private List<StoreEntranceResponse> toResponses(List<StoreEntrance> entrances) {
    return entrances.stream().map(this::toResponse).toList();
  }

  private StoreEntranceResponse toResponse(StoreEntrance entrance) {
    return new StoreEntranceResponse(
        entrance.storeId(),
        storeCatalog.findById(entrance.storeId()).map(Store::name).orElse(null),
        entrance.enteredAt(),
        entrance.geoPoint().lat(),
        entrance.geoPoint().lng(),
        entrance.distanceToStoreMeters());
  }
}
