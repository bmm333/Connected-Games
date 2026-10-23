package com.connectedgames.tournament;

import org.springframework.boot.SpringApplication;

public class TestTournamentApplication {

	public static void main(String[] args) {
		SpringApplication.from(TournamentApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
