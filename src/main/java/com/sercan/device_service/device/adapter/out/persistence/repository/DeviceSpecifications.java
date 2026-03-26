package com.sercan.device_service.device.adapter.out.persistence.repository;

import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.domain.model.DeviceState;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import static org.apache.commons.lang3.StringUtils.isBlank;

@NoArgsConstructor
public class DeviceSpecifications {

    public static Specification<DeviceJpaEntity> byFilter(DeviceFilter filter) {
        Specification<DeviceJpaEntity> spec = Specification.unrestricted();

        if (!isBlank(filter.name())) {
            spec = spec.and(hasName(filter.name()));
        }

        if (!isBlank(filter.brand())) {
            spec = spec.and(hasBrand(filter.brand()));
        }

        if (filter.state() != null) {
            spec = spec.and(hasState(filter.state()));
        }

        return spec;
    }

    private static Specification<DeviceJpaEntity> hasName(String name) {
        String normalizedName = name.trim().toLowerCase();

        return (root, query, cb) ->
                cb.equal(
                        cb.lower(root.get("name")),
                        normalizedName
                );
    }

    private static Specification<DeviceJpaEntity> hasBrand(String brand) {
        String normalizedBrand = brand.trim().toLowerCase();

        return (root, query, cb) ->
                cb.equal(
                        cb.lower(root.get("brand")),
                        normalizedBrand
                );
    }

    private static Specification<DeviceJpaEntity> hasState(DeviceState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }
}
