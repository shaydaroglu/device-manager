package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.device.adapter.out.persistence.repository.DeviceJpaRepository;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.port.out.DevicePersistencePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class DevicePersistenceAdapter implements DevicePersistencePort {

    private final DeviceJpaRepository deviceJpaRepository;
    private final DevicePersistenceMapper devicePersistenceMapper;

    @Override
    public Device save(Device device) {
        var entity = devicePersistenceMapper.toEntity(device);
        var savedEntity = deviceJpaRepository.save(entity);
        return devicePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public java.util.Optional<Device> findById(java.util.UUID id) {
        return deviceJpaRepository.findById(id)
                .map(devicePersistenceMapper::toDomain);
    }

    @Override
    public java.util.List<Device> findAll() {
        return deviceJpaRepository.findAll().stream()
                .map(devicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public java.util.List<Device> findByBrand(String brand) {
        return deviceJpaRepository.findByBrandIgnoreCase(brand).stream()
                .map(devicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public java.util.List<Device> findByState(com.sercan.device_service.device.domain.model.DeviceState state) {
        return deviceJpaRepository.findByState(state).stream()
                .map(devicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        deviceJpaRepository.deleteById(id);
    }
}
