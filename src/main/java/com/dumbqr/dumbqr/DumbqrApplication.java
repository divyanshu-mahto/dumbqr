package com.dumbqr.dumbqr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DumbqrApplication {

	public static void main(String[] args) {
		SpringApplication.run(DumbqrApplication.class, args);
	}

}
