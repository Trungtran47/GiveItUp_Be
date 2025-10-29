package com.giveitup.giveitup_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // ✅ Bật tính năng auditing
public class GiveItUpBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiveItUpBeApplication.class, args);
	}

}
