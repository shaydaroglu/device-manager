package com.sercan.device_service;

import org.springframework.boot.SpringApplication;

public class TestDeviceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeviceServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
