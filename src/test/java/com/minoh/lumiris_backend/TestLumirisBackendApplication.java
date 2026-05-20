package com.minoh.lumiris_backend;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestLumirisBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(LumirisBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
