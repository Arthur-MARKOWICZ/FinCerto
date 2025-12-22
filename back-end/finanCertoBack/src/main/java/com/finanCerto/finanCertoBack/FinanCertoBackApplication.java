package com.finanCerto.finanCertoBack;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanCertoBackApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("FAST_API_URL", dotenv.get("FASTAPI_BASE_URL"));
		SpringApplication.run(FinanCertoBackApplication.class, args);
	}

}
