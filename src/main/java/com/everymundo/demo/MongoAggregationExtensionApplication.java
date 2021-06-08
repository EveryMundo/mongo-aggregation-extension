package com.everymundo.demo;

import com.everymundo.demo.service.DataService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MongoAggregationExtensionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoAggregationExtensionApplication.class, args);
	}

	@Bean
	CommandLineRunner populateData(DataService dataService) {
		return args -> dataService.populateData().subscribe();
	}

}
