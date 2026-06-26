package com.coderank.profile.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class CodeforcesClient {

    private final WebClient webClient;

    public CodeforcesClient(WebClient.Builder webClientBuilder,
                            @Value("${app.api.codeforces-url}") String codeforcesApiUrl) {
        this.webClient = webClientBuilder.baseUrl(codeforcesApiUrl).build();
    }

    public boolean userExists(String handle) {
        try {
            Map response = webClient.get()
                    .uri("/user.info?handles={handle}", handle)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null && "OK".equals(response.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> fetchStats(String handle) {
        try {
            Map response = webClient.get()
                    .uri("/user.info?handles={handle}", handle)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
                if (result != null && !result.isEmpty()) {
                    return result.get(0);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
