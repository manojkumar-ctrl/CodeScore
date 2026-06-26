package com.coderank.profile.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class LeetcodeClient {

    private final WebClient webClient;

    public LeetcodeClient(WebClient.Builder webClientBuilder,
                          @Value("${app.api.leetcode-url}") String leetcodeApiUrl) {
        this.webClient = webClientBuilder.baseUrl(leetcodeApiUrl).build();
    }

    public boolean userExists(String username) {
        String query = "query getUser($username: String!) { matchedUser(username: $username) { username } }";
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        try {
            Map response = webClient.post()
                    .uri("/graphql")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                Map data = (Map) response.get("data");
                return data != null && data.get("matchedUser") != null;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> fetchStats(String username) {
        String query = "query getUserProfile($username: String!) { " +
                "matchedUser(username: $username) { " +
                "username profile { ranking reputation } " +
                "submitStats: submitStatsGlobal { acSubmissionNum { difficulty count } } } " +
                "userContestRanking(username: $username) { rating globalRanking attendedContestsCount } }";
        
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        try {
            return webClient.post()
                    .uri("/graphql")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }
}
