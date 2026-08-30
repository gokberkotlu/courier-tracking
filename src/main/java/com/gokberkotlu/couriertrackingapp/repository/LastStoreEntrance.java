package com.gokberkotlu.couriertrackingapp.repository;

import java.time.Instant;

public record LastStoreEntrance(Long storeId, Instant enteredAt) {}
