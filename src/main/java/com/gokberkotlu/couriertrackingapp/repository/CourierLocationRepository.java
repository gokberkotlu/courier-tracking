package com.gokberkotlu.couriertrackingapp.repository;

import com.gokberkotlu.couriertrackingapp.entity.CourierLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierLocationRepository extends JpaRepository<CourierLocationEntity, Long> {}
