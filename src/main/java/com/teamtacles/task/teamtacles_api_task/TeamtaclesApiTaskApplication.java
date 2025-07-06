package com.teamtacles.task.teamtacles_api_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "TeamTacles API Task", version = "1.0", description = "TeamTacles Task API Documentation – Task Management"))
@SpringBootApplication
public class TeamtaclesApiTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamtaclesApiTaskApplication.class, args);
	}

}
