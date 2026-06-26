package com.coderank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@EnableCaching
@SpringBootApplication
public class CoderankBackendApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(CoderankBackendApplication.class, args);
	}

	/**
	 * Loads environment variables from .env into System properties
	 * before Spring context starts. Works regardless of CWD or IDE.
	 */
	private static void loadEnv() {
		// Candidate locations: project root and one level up
		String[] candidates = {".env", "coderank-backend/.env"};
		for (String path : candidates) {
			File envFile = new File(path);
			if (envFile.exists()) {
				try {
					List<String> lines = Files.readAllLines(envFile.toPath());
					for (String line : lines) {
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#")) continue;
						int eq = line.indexOf('=');
						if (eq > 0) {
							String key   = line.substring(0, eq).trim();
							String value = line.substring(eq + 1).trim();
							// Strip surrounding quotes
							if (value.length() >= 2
									&& ((value.startsWith("\"") && value.endsWith("\""))
									||  (value.startsWith("'")  && value.endsWith("'")))) {
								value = value.substring(1, value.length() - 1);
							}
							// Only set if not already provided (e.g. via -D JVM arg)
							if (System.getProperty(key) == null) {
								System.setProperty(key, value);
							}
						}
					}
					System.out.println("[EnvLoader] Loaded .env from: " + envFile.getAbsolutePath());
					return;
				} catch (IOException e) {
					System.err.println("[EnvLoader] Failed to read " + path + ": " + e.getMessage());
				}
			}
		}
		System.err.println("[EnvLoader] WARNING: No .env file found — ensure environment variables are set externally.");
	}
}
