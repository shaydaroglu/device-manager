package com.sercan.device_service.device.adapter.in.rest;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message) {
}
