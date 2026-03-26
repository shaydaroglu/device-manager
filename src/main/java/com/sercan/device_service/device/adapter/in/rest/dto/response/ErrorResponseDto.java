package com.sercan.device_service.device.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned by the API"
)
public record ErrorResponseDto(
        @Schema(
                description = "Error timestamp in UTC",
                example = "2026-03-26T00:54:16.693163200Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "Human-readable error message"
        )
        String message) {
}
