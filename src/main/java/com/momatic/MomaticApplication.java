package com.momatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MomaticApplication {

	public static void main(String[] args) {
		SpringApplication.run(MomaticApplication.class, args);
	}
}