package com.cadence;

import org.springframework.boot.SpringApplication;

public class TestCadenceApplication {

	public static void main(String[] args) {
		SpringApplication.from(CadenceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
