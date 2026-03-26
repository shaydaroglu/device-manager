package com.sercan.device_service.device.adapter.out.persistence.repository;

import com.sercan.device_service.device.adapter.in.rest.dto.request.DeviceFilter;
import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.domain.model.DeviceState;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import static org.apache.commons.lang3.StringUtils.isBlank;

@NoArgsConstructor
public class DeviceSpecifications {

    public static Specification<DeviceJpaEntity> byFilter(DeviceFilter filter) {
        Specification<DeviceJpaEntity> spec = Specification.unrestricted();

        if(!isBlank(filter.name())) {
            spec = spec.and(hasName(filter.name()));
        }

        if(!isBlank(filter.brand())) {
            spec = spec.and(hasBrand(filter.brand()));
        }

        if(filter.state() != null) {
            spec = spec.and(hasState(filter.state()));
        }

        return spec;
    }


    private static Specification<DeviceJpaEntity> hasName(String name) {
        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("name")),
                        name.trim().toUpperCase()
                );
    }

    private static Specification<DeviceJpaEntity> hasBrand(String brand) {
        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("brand")),
                        brand.trim().toUpperCase()
                );
    }

    private static Specification<DeviceJpaEntity> hasState(DeviceState state) {
        return (root, query, cb) ->
                cb.equal(root.get("state"), state);
    }
}
