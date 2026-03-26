package com.sercan.device_service.device.domain.port.out;

import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.domain.model.Device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevicePersistencePort {
    Device save(Device device);
    Optional<Device> findById(UUID id);
    List<Device> findAll();
    List<Device> findByFilter(DeviceFilter filter);
    void deleteById(UUID id);
}
