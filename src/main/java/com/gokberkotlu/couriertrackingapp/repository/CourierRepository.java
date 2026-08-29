package com.gokberkotlu.couriertrackingapp.repository;

import com.gokberkotlu.couriertrackingapp.entity.CourierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<CourierEntity, Long> {}
