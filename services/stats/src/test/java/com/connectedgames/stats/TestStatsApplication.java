package com.connectedgames.stats;

import org.springframework.boot.SpringApplication;

public class TestStatsApplication {

	public static void main(String[] args) {
		SpringApplication.from(StatsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
