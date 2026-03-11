package com.bombazine.setlist_builder;

import com.bombazine.setlist_builder.dto.RegisterRequest;
import com.bombazine.setlist_builder.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SetlistBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SetlistBuilderApplication.class, args);
	}

}
