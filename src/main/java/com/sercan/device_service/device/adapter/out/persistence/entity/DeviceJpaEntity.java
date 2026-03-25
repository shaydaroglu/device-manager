package com.sercan.device_service.device.adapter.out.persistence.entity;

import com.sercan.device_service.device.domain.model.DeviceState;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
public class DeviceJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceState state;

    @Column(name = "creation_time", nullable = false, updatable = false, insertable = false)
    private Instant creationTime;

    @Column(name = "update_time", nullable = false, updatable = false, insertable = false)
    private Instant updateTime;

    @Builder
    public DeviceJpaEntity(UUID id, String name, String brand, DeviceState state) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
    }
}
