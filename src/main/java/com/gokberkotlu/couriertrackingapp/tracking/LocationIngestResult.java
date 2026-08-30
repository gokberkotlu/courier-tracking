package com.gokberkotlu.couriertrackingapp.tracking;

import com.gokberkotlu.couriertrackingapp.model.StoreEntrance;
import java.util.List;

public record LocationIngestResult(
    boolean outlier, double totalDistanceMeters, List<StoreEntrance> storeEntrances) {}
