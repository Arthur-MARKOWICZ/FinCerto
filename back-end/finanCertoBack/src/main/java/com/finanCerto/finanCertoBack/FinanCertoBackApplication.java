package com.finanCerto.finanCertoBack;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanCertoBackApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.load();
			
			String dbUsername = dotenv.get("DB_USERNAME");
			String dbPassword = dotenv.get("DB_PASSWORD");
			String jwtSecret = dotenv.get("JWT_SECRET");
			String fastApiUrl = dotenv.get("FASTAPI_BASE_URL");
			
			if (dbUsername != null) System.setProperty("DB_USERNAME", dbUsername);
			if (dbPassword != null) System.setProperty("DB_PASSWORD", dbPassword);
			if (jwtSecret != null) System.setProperty("JWT_SECRET", jwtSecret);
			if (fastApiUrl != null) System.setProperty("FAST_API_URL", fastApiUrl);
		} catch (Exception e) {
			System.out.println("Warning: Could not load .env file. Using default configuration or environment variables.");
		}
		
		SpringApplication.run(FinanCertoBackApplication.class, args);
	}

}
