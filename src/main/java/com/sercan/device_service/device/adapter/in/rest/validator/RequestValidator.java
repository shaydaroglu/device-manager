package com.sercan.device_service.device.adapter.in.rest.validator;

import com.sercan.device_service.device.adapter.in.rest.dto.request.PatchDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.exception.PatchValidationException;
import com.sercan.device_service.device.adapter.in.rest.exception.SearchFilterValidationException;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
public class RequestValidator {

    public void validatePatch(PatchDeviceRequestDto request) {
        if (request == null ||
            (request.name() == null && request.brand() == null && request.state() == null)) {
            throw new PatchValidationException("At least one field must be provided for patching");
        }
    }

    public void validateSearchFilter(String name, String brand, DeviceState state) {
        if (StringUtils.isBlank(name) && StringUtils.isBlank(brand) && state == null) {
            throw new SearchFilterValidationException("At least one search filter must be provided");
        }
    }
}


