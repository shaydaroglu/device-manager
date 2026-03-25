package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.domain.model.Device;
import org.springframework.stereotype.Component;

@Component
public class DevicePersistenceMapper {

    public Device toDomain(DeviceJpaEntity entity) {
        return new Device(
                entity.getId(),
                entity.getName(),
                entity.getBrand(),
                entity.getState(),
                entity.getCreationTime(),
                entity.getUpdateTime()
        );
    }

    public DeviceJpaEntity toEntity(Device device) {
        return new DeviceJpaEntity(
                device.id(),
                device.name(),
                device.brand(),
                device.state()
        );
    }
}
