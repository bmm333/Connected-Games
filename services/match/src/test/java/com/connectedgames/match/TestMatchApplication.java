package com.connectedgames.match;

import org.springframework.boot.SpringApplication;

public class TestMatchApplication {

	public static void main(String[] args) {
		SpringApplication.from(MatchApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
