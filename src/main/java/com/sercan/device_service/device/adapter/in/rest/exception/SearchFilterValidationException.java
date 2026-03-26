package com.sercan.device_service.device.adapter.in.rest.exception;

public class SearchFilterValidationException extends RuntimeException {
    public SearchFilterValidationException() {
        super("At least one search filter must be provided");
    }
}
