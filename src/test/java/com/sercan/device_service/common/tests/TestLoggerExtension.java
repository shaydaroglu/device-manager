package com.sercan.device_service.common.tests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TestLoggerExtension implements TestWatcher {
    private static final Logger log = LoggerFactory.getLogger(TestLoggerExtension.class);

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.info("Test passed: {}", context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        log.error("Test failed: {}", context.getDisplayName(), cause);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        log.warn("Test disabled: {}", context.getDisplayName());
    }
}
