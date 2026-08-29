package com.gokberkotlu.couriertrackingapp.model;

import java.time.Instant;

public record StoreEntrance(
    Long courierId,
    Long storeId,
    Instant enteredAt,
    GeoPoint geoPoint,
    double distanceToStoreMeters) {}
