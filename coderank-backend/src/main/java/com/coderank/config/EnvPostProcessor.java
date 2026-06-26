package com.coderank.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("=== EnvPostProcessor: Running environment post processor ===");
        File envFile = new File(".env");
        System.out.println("Checking .env in: " + envFile.getAbsolutePath());
        if (!envFile.exists()) {
            envFile = new File("coderank-backend/.env");
            System.out.println("Checking .env in: " + envFile.getAbsolutePath());
        }

        if (envFile.exists()) {
            System.out.println("=== Found .env file at: " + envFile.getAbsolutePath());
            try {
                List<String> lines = Files.readAllLines(Paths.get(envFile.getAbsolutePath()));
                Map<String, Object> envProperties = new HashMap<>();
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        envProperties.put(key, value);
                        System.out.println("Loaded property: " + key + " = "
                                + (key.contains("PASSWORD") || key.contains("SECRET") ? "********" : value));
                    }
                }
                if (!envProperties.isEmpty()) {
                    environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envProperties));
                    System.out.println("=== Added dotenvProperties MapPropertySource ===");
                }
            } catch (IOException e) {
                System.err.println("Failed to load .env file: " + e.getMessage());
            }
        } else {
            System.out.println("=== .env file not found anywhere! ===");
        }
    }
}
