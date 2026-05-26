package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling

public class DemoApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		// Set variables to System properties so Spring finds them
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("GOLD_API_KEY", dotenv.get("GOLD_API_KEY"));

		SpringApplication.run(DemoApplication.class, args);
	}

}
