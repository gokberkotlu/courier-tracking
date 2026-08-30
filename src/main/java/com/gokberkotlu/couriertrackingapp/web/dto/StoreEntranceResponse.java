package com.gokberkotlu.couriertrackingapp.web.dto;

import java.time.Instant;

public record StoreEntranceResponse(
    Long storeId,
    String storeName,
    Instant enteredAt,
    double lat,
    double lng,
    double distanceToStoreMeters) {}
