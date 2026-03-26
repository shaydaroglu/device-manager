package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.adapter.out.persistence.repository.DeviceJpaRepository;
import com.sercan.device_service.device.adapter.out.persistence.repository.DeviceSpecifications;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.port.out.DevicePersistencePort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class DevicePersistenceAdapter implements DevicePersistencePort {

    private final DeviceJpaRepository deviceJpaRepository;
    private final DevicePersistenceMapper devicePersistenceMapper;

    @Override
    public Device save(Device device) {
        log.debug("Persisting device (id='{}', name='{}', brand='{}')", device.id(), device.name(), device.brand());
        DeviceJpaEntity entity = deviceJpaRepository.findById(device.id())
                .map(existing -> {
                    existing.setName(device.name());
                    existing.setBrand(device.brand());
                    existing.setState(device.state());
                    return existing;
                })
                .orElseGet(() -> devicePersistenceMapper.toEntity(device));

        DeviceJpaEntity savedEntity = deviceJpaRepository.save(entity);

        DeviceJpaEntity reloadedEntity = deviceJpaRepository.findById(savedEntity.getId())
                .orElseThrow(() -> {
                    log.error("Device was saved but could not be reloaded, id='{}'", savedEntity.getId());
                    return new IllegalStateException("Saved device could not be reloaded");
                });

        log.debug("Device persisted successfully with id='{}'", reloadedEntity.getId());
        return devicePersistenceMapper.toDomain(reloadedEntity);
    }

    @Override
    public Optional<Device> findById(UUID id) {
        return deviceJpaRepository.findById(id)
                .map(devicePersistenceMapper::toDomain);
    }

    @Override
    public List<Device> findAll() {
        return deviceJpaRepository.findAll().stream()
                .map(devicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Device> findByFilter(DeviceFilter filter) {
        return deviceJpaRepository.findAll(DeviceSpecifications.byFilter(filter))
                .stream()
                .map(devicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        deviceJpaRepository.deleteById(id);
    }
}
