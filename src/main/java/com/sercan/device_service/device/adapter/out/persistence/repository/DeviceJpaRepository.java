package com.sercan.device_service.device.adapter.out.persistence.repository;

import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, UUID> {
    List<DeviceJpaEntity> findByBrandIgnoreCase(String brand);
    List<DeviceJpaEntity> findByState(DeviceState state);
}
