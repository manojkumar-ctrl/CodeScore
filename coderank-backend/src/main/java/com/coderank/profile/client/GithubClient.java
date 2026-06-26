package com.coderank.profile.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GithubClient {

    private final WebClient webClient;

    public GithubClient(WebClient.Builder webClientBuilder,
                        @Value("${app.api.github-url}") String githubApiUrl) {
        this.webClient = webClientBuilder.baseUrl(githubApiUrl).build();
    }

    public boolean userExists(String username) {
        try {
            return Boolean.TRUE.equals(webClient.get()
                    .uri("/users/{username}", username)
                    .exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.OK)) {
                            return Mono.just(true);
                        } else {
                            return Mono.just(false);
                        }
                    })
                    .block());
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> fetchProfile(String username) {
        try {
            return webClient.get()
                    .uri("/users/{username}", username)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    public Integer fetchTotalCommits(String username) {
        try {
            Map response = webClient.get()
                    .uri("/search/commits?q=author:{username}", username)
                    .header("Accept", "application/vnd.github.cloak-preview")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("total_count")) {
                Object count = response.get("total_count");
                if (count instanceof Number) {
                    return ((Number) count).intValue();
                }
            }
            return 0;
        } catch (Exception e) {
            return 0; // Search API has rate limits, fallback to 0 if it fails
        }
    }
}
