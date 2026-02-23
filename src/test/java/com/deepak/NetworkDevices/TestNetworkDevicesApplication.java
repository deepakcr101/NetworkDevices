package com.deepak.NetworkDevices;

import org.springframework.boot.SpringApplication;

public class TestNetworkDevicesApplication {

	public static void main(String[] args) {
		SpringApplication.from(NetworkDevicesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
