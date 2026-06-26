package com.coderank.profile.service;

import com.coderank.profile.client.CodeforcesClient;
import com.coderank.profile.client.GithubClient;
import com.coderank.profile.client.LeetcodeClient;
import com.coderank.profile.dto.ConnectProfileRequest;
import com.coderank.profile.dto.ConnectProfileResponse;
import com.coderank.profile.dto.ProfileResponse;
import com.coderank.profile.entity.CodeforcesStats;
import com.coderank.profile.entity.CodingProfile;
import com.coderank.profile.entity.GithubStats;
import com.coderank.profile.entity.LeetcodeStats;
import com.coderank.profile.repository.CodeforcesStatsRepository;
import com.coderank.profile.repository.CodingProfileRepository;
import com.coderank.profile.repository.GithubStatsRepository;
import com.coderank.profile.repository.LeetcodeStatsRepository;
import com.coderank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CodingProfileRepository codingProfileRepository;
    private final GithubStatsRepository githubStatsRepository;
    private final LeetcodeStatsRepository leetcodeStatsRepository;
    private final CodeforcesStatsRepository codeforcesStatsRepository;

    private final GithubClient githubClient;
    private final LeetcodeClient leetcodeClient;
    private final CodeforcesClient codeforcesClient;

    private String extractUsername(String input) {
        if (input == null || input.isBlank()) return null;
        if (input.startsWith("http")) {
            if (input.endsWith("/")) input = input.substring(0, input.length() - 1);
            String[] parts = input.split("/");
            return parts[parts.length - 1];
        }
        return input;
    }

    public ConnectProfileResponse connectProfiles(User user, ConnectProfileRequest request) {
        
        String githubUsername = extractUsername(request.getGithubUsername());
        String leetcodeUsername = extractUsername(request.getLeetcodeUsername());
        String codeforcesHandle = extractUsername(request.getCodeforcesHandle());
        
        // 1. Validate GitHub
        boolean githubVerified = githubClient.userExists(githubUsername);
        if (!githubVerified) {
            throw new RuntimeException("GitHub profile not found");
        }

        // 2. Validate LeetCode
        boolean leetcodeVerified = leetcodeClient.userExists(leetcodeUsername);
        if (!leetcodeVerified) {
            throw new RuntimeException("LeetCode profile not found");
        }

        // 3. Validate Codeforces (optional)
        Boolean codeforcesVerified = null;
        if (codeforcesHandle != null && !codeforcesHandle.isBlank()) {
            codeforcesVerified = codeforcesClient.userExists(codeforcesHandle);
        }

        // 4. Save CodingProfile
        CodingProfile profile = codingProfileRepository.findByUser(user).orElse(new CodingProfile());
        profile.setUser(user);
        profile.setGithubUsername(githubUsername);
        profile.setLeetcodeUsername(leetcodeUsername);
        profile.setCodeforcesHandle(codeforcesHandle);
        codingProfileRepository.save(profile);

        // 5. Fetch and store latest stats async/sync
        updateGithubStats(user, githubUsername);
        updateLeetcodeStats(user, leetcodeUsername);
        if (Boolean.TRUE.equals(codeforcesVerified)) {
            updateCodeforcesStats(user, codeforcesHandle);
        }

        return ConnectProfileResponse.builder()
                .message("Profiles connected successfully")
                .profileConnected(true)
                .githubVerified(githubVerified)
                .leetcodeVerified(leetcodeVerified)
                .codeforcesVerified(codeforcesVerified)
                .build();
    }

    public ProfileResponse getProfiles(User user) {
        CodingProfile profile = codingProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No profiles connected"));

        return ProfileResponse.builder()
                .githubUsername(profile.getGithubUsername())
                .leetcodeUsername(profile.getLeetcodeUsername())
                .codeforcesHandle(profile.getCodeforcesHandle())
                .build();
    }

    public void syncProfiles(User user) {
        CodingProfile profile = codingProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No profiles connected"));
        if (profile.getGithubUsername() != null && !profile.getGithubUsername().isBlank()) {
            updateGithubStats(user, profile.getGithubUsername());
        }
        if (profile.getLeetcodeUsername() != null && !profile.getLeetcodeUsername().isBlank()) {
            updateLeetcodeStats(user, profile.getLeetcodeUsername());
        }
        if (profile.getCodeforcesHandle() != null && !profile.getCodeforcesHandle().isBlank()) {
            updateCodeforcesStats(user, profile.getCodeforcesHandle());
        }
    }

    private void updateGithubStats(User user, String username) {
        Map<String, Object> data = githubClient.fetchProfile(username);
        if (data != null) {
            GithubStats stats = githubStatsRepository.findByUser(user).orElse(new GithubStats());
            stats.setUser(user);
            stats.setFollowers((Integer) data.getOrDefault("followers", 0));
            stats.setFollowing((Integer) data.getOrDefault("following", 0));
            stats.setPublicRepos((Integer) data.getOrDefault("public_repos", 0));
            stats.setStars(0); // Stars requires another API call, leaving 0 for now
            stats.setTotalCommits(githubClient.fetchTotalCommits(username));
            stats.setLastUpdated(LocalDateTime.now());
            githubStatsRepository.save(stats);
        }
    }

    private void updateLeetcodeStats(User user, String username) {
        Map<String, Object> response = leetcodeClient.fetchStats(username);
        if (response != null && response.containsKey("data")) {
            Map data = (Map) response.get("data");
            Map matchedUser = (Map) data.get("matchedUser");
            Map userContestRanking = (Map) data.get("userContestRanking");

            LeetcodeStats stats = leetcodeStatsRepository.findByUser(user).orElse(new LeetcodeStats());
            stats.setUser(user);
            stats.setLeetcodeUsername(username);

            if (matchedUser != null) {
                Map profile = (Map) matchedUser.get("profile");
                if (profile != null) {
                    Object rankingObj = profile.get("ranking");
                    if (rankingObj instanceof Number) stats.setRanking(((Number) rankingObj).intValue());
                    
                    Object reputationObj = profile.get("reputation");
                    if (reputationObj instanceof Number) stats.setReputation(((Number) reputationObj).intValue());
                }

                Map submitStats = (Map) matchedUser.get("submitStats");
                if (submitStats != null) {
                    List<Map<String, Object>> acSubmissionNum = (List<Map<String, Object>>) submitStats.get("acSubmissionNum");
                    if (acSubmissionNum != null) {
                        for (Map<String, Object> item : acSubmissionNum) {
                            String difficulty = (String) item.get("difficulty");
                            Integer count = (Integer) item.get("count");
                            if ("All".equals(difficulty)) stats.setTotalSolved(count);
                            else if ("Easy".equals(difficulty)) stats.setEasySolved(count);
                            else if ("Medium".equals(difficulty)) stats.setMediumSolved(count);
                            else if ("Hard".equals(difficulty)) stats.setHardSolved(count);
                        }
                    }
                }
            }

            if (userContestRanking != null) {
                Object ratingObj = userContestRanking.get("rating");
                if (ratingObj instanceof Number) {
                    stats.setContestRating(((Number) ratingObj).doubleValue());
                }
                Object globalRankingObj = userContestRanking.get("globalRanking");
                if (globalRankingObj instanceof Number) {
                    stats.setContestGlobalRanking(((Number) globalRankingObj).intValue());
                }
                Object countObj = userContestRanking.get("attendedContestsCount");
                if (countObj instanceof Number) {
                    stats.setContestsAttended(((Number) countObj).intValue());
                }
            }

            stats.setLastSyncedAt(LocalDateTime.now());
            leetcodeStatsRepository.save(stats);
        }
    }

    private void updateCodeforcesStats(User user, String handle) {
        Map<String, Object> data = codeforcesClient.fetchStats(handle);
        if (data != null) {
            CodeforcesStats stats = codeforcesStatsRepository.findByUser(user).orElse(new CodeforcesStats());
            stats.setUser(user);
            stats.setRating((Integer) data.getOrDefault("rating", 0));
            stats.setMaxRating((Integer) data.getOrDefault("maxRating", 0));
            stats.setRankName((String) data.get("rank"));
            stats.setMaxRank((String) data.get("maxRank"));
            stats.setLastUpdated(LocalDateTime.now());
            codeforcesStatsRepository.save(stats);
        }
    }
}
