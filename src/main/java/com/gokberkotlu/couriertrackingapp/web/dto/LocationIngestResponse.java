package com.gokberkotlu.couriertrackingapp.web.dto;

import java.util.List;

public record LocationIngestResponse(
    boolean accepted, double totalDistanceMeters, List<StoreEntranceResponse> storeEntrances) {}
