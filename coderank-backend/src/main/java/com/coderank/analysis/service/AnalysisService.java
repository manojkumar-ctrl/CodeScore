package com.coderank.analysis.service;

import com.coderank.analysis.dto.AnalysisResponse;
import com.coderank.analysis.dto.CompareResponse;
import com.coderank.analysis.dto.DashboardResponse;
import com.coderank.analysis.dto.LeaderboardEntry;
import com.coderank.analysis.entity.AnalysisResult;
import com.coderank.analysis.mapper.AnalysisMapper;
import com.coderank.analysis.repository.AnalysisResultRepository;
import com.coderank.profile.entity.GithubStats;
import com.coderank.profile.entity.LeetcodeStats;
import com.coderank.profile.repository.GithubStatsRepository;
import com.coderank.profile.repository.LeetcodeStatsRepository;
import com.coderank.profile.service.ProfileService;
import com.coderank.user.entity.User;
import com.coderank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ProfileService profileService;
    private final GithubStatsRepository githubStatsRepository;
    private final LeetcodeStatsRepository leetcodeStatsRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisMapper analysisMapper;
    private final UserRepository userRepository;

    /**
     * Runs a full analysis: syncs external profiles, calculates scores,
     * saves to DB, and evicts the dashboard cache so the next fetch is fresh.
     *
     * Cache-Aside Pattern:
     *   runAnalysis (refresh) → DB write → evict cache → next request rebuilds cache
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboard", key = "#user.id"),   // evict this user's dashboard
            @CacheEvict(value = "leaderboard", allEntries = true)  // evict all leaderboards (rankings changed)
    })
    public AnalysisResponse runAnalysis(User user) {
        // 1. Sync profile statistics from external APIs
        profileService.syncProfiles(user);

        // 2. Fetch current statistics from the database
        GithubStats githubStats = githubStatsRepository.findByUser(user).orElse(null);
        LeetcodeStats leetcodeStats = leetcodeStatsRepository.findByUser(user).orElse(null);

        if (githubStats == null && leetcodeStats == null) {
            throw new RuntimeException("No connected profiles found. Please connect LeetCode or GitHub profiles first.");
        }

        // 3. Calculate scores
        double dsaScore = calculateDsaScore(leetcodeStats);
        double contestScore = calculateContestScore(leetcodeStats);
        double githubScore = calculateGithubScore(githubStats);
        double developerScore = calculateDeveloperScore(dsaScore, githubScore, contestScore);
        double consistencyScore = 0.0; // Placeholder / future expansion

        // 4. Save results to the database
        AnalysisResult result = AnalysisResult.builder()
                .user(user)
                .dsaScore(dsaScore)
                .githubScore(githubScore)
                .contestScore(contestScore)
                .developerScore(developerScore)
                .consistencyScore(consistencyScore)
                .build();

        analysisResultRepository.save(result);

        // 5. Readiness Classification & Strengths/Weaknesses
        String classification = getReadinessClassification(developerScore);
        List<String> strengths = generateStrengths(leetcodeStats, githubStats);
        List<String> weaknesses = generateWeaknesses(leetcodeStats, githubStats);

        return analysisMapper.toAnalysisResponse(result, classification, strengths, weaknesses);
    }

    /**
     * Get the latest saved analysis result without re-running.
     */
    public AnalysisResponse getLatestAnalysis(User user) {
        AnalysisResult latestResult = analysisResultRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("No analysis found. Please run an analysis first."));

        GithubStats githubStats = githubStatsRepository.findByUser(user).orElse(null);
        LeetcodeStats leetcodeStats = leetcodeStatsRepository.findByUser(user).orElse(null);

        String classification = getReadinessClassification(latestResult.getDeveloperScore());
        List<String> strengths = generateStrengths(leetcodeStats, githubStats);
        List<String> weaknesses = generateWeaknesses(leetcodeStats, githubStats);

        return analysisMapper.toAnalysisResponse(latestResult, classification, strengths, weaknesses);
    }

    /**
     * Returns the dashboard for a user.
     *
     * Cache-Aside Pattern:
     *   First request  → Cache MISS → query PostgreSQL → store in Redis → return
     *   Second request → Cache HIT  → return from Redis instantly (no DB query)
     *   After refresh  → @CacheEvict deletes Redis entry → next call re-fetches from DB
     */
    @Cacheable(value = "dashboard", key = "#user.id")
    public DashboardResponse getDashboard(User user) {
        // Fetch latest saved analysis result
        AnalysisResult latestResult = analysisResultRepository.findFirstByUserOrderByCreatedAtDesc(user).orElse(null);

        // Fetch current statistics
        GithubStats githubStats = githubStatsRepository.findByUser(user).orElse(null);
        LeetcodeStats leetcodeStats = leetcodeStatsRepository.findByUser(user).orElse(null);

        // If no analysis result exists, attempt to run a new analysis on the fly
        if (latestResult == null) {
            if (githubStats != null || leetcodeStats != null) {
                // Run analysis on the fly
                try {
                    AnalysisResponse response = runAnalysis(user);
                    latestResult = analysisResultRepository.findFirstByUserOrderByCreatedAtDesc(user).orElse(null);
                } catch (Exception e) {
                    // Log error and fallback
                }
            }
        }

        double devScore = latestResult != null ? latestResult.getDeveloperScore() : 0.0;
        String classification = getReadinessClassification(devScore);

        return analysisMapper.toDashboardResponse(latestResult, leetcodeStats, githubStats, classification);
    }

    /**
     * Get leaderboard — top users ranked by the given category.
     * Cached per category string (e.g., "dashboard", "dsa", "github", "contest").
     */
    @Cacheable(value = "leaderboard", key = "#category")
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(String category) {
        List<AnalysisResult> results = switch (category.toLowerCase()) {
            case "dsa" -> analysisResultRepository.findLatestPerUserOrderByDsaScoreDesc();
            case "github" -> analysisResultRepository.findLatestPerUserOrderByGithubScoreDesc();
            case "contest" -> analysisResultRepository.findLatestPerUserOrderByContestScoreDesc();
            default -> analysisResultRepository.findLatestPerUserOrderByDeveloperScoreDesc();
        };

        // Limit to top 10 and map to LeaderboardEntry
        return IntStream.range(0, Math.min(results.size(), 10))
                .mapToObj(i -> {
                    AnalysisResult r = results.get(i);
                    double score = switch (category.toLowerCase()) {
                        case "dsa" -> r.getDsaScore() != null ? r.getDsaScore() : 0.0;
                        case "github" -> r.getGithubScore() != null ? r.getGithubScore() : 0.0;
                        case "contest" -> r.getContestScore() != null ? r.getContestScore() : 0.0;
                        default -> r.getDeveloperScore() != null ? r.getDeveloperScore() : 0.0;
                    };
                    return LeaderboardEntry.builder()
                            .rank(i + 1)
                            .username(r.getUser().getName())
                            .score(score)
                            .category(category)
                            .build();
                })
                .toList();
    }

    /**
     * Compare the current user's latest analysis with another user's.
     */
    @Transactional(readOnly = true)
    public CompareResponse compareWith(User currentUser, String identifier) {
        // Try to find the friend by name or email
        User friend = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByName(identifier)
                        .orElseThrow(() -> new RuntimeException("User not found: " + identifier)));

        // Get latest analysis for both
        AnalysisResult myResult = analysisResultRepository.findFirstByUserOrderByCreatedAtDesc(currentUser)
                .orElseThrow(() -> new RuntimeException("You don't have any analysis yet. Run an analysis first."));

        AnalysisResult friendResult = analysisResultRepository.findFirstByUserOrderByCreatedAtDesc(friend)
                .orElseThrow(() -> new RuntimeException("The other user hasn't run any analysis yet."));

        // Get stats for both
        LeetcodeStats myLc = leetcodeStatsRepository.findByUser(currentUser).orElse(null);
        GithubStats myGh = githubStatsRepository.findByUser(currentUser).orElse(null);
        LeetcodeStats friendLc = leetcodeStatsRepository.findByUser(friend).orElse(null);
        GithubStats friendGh = githubStatsRepository.findByUser(friend).orElse(null);

        return CompareResponse.builder()
                .me(buildScoreData(myResult, myLc, myGh))
                .friend(buildScoreData(friendResult, friendLc, friendGh))
                .build();
    }

    private CompareResponse.ScoreData buildScoreData(AnalysisResult result, LeetcodeStats lc, GithubStats gh) {
        return CompareResponse.ScoreData.builder()
                .developerScore(result.getDeveloperScore() != null ? result.getDeveloperScore() : 0.0)
                .dsaScore(result.getDsaScore() != null ? result.getDsaScore() : 0.0)
                .githubScore(result.getGithubScore() != null ? result.getGithubScore() : 0.0)
                .contestScore(result.getContestScore() != null ? result.getContestScore() : 0.0)
                .classification(getReadinessClassification(result.getDeveloperScore() != null ? result.getDeveloperScore() : 0.0))
                .leetcodeSolved(lc != null && lc.getTotalSolved() != null ? lc.getTotalSolved() : 0)
                .contestRating(lc != null && lc.getContestRating() != null ? lc.getContestRating() : 0.0)
                .githubRepos(gh != null && gh.getPublicRepos() != null ? gh.getPublicRepos() : 0)
                .build();
    }

    // DSA Score Calculation
    public double calculateDsaScore(LeetcodeStats stats) {
        if (stats == null) {
            return 0.0;
        }
        int easy = stats.getEasySolved() != null ? stats.getEasySolved() : 0;
        int medium = stats.getMediumSolved() != null ? stats.getMediumSolved() : 0;
        int hard = stats.getHardSolved() != null ? stats.getHardSolved() : 0;

        double rawScore = (easy * 1.0) + (medium * 2.0) + (hard * 4.0);
        return Math.min((rawScore / 800.0) * 100.0, 100.0);
    }

    // Contest Score Calculation
    public double calculateContestScore(LeetcodeStats stats) {
        if (stats == null) {
            return 0.0;
        }
        Double rating = stats.getContestRating();
        Integer contests = stats.getContestsAttended();

        if (rating == null || rating <= 0.0 || contests == null || contests <= 0) {
            return 0.0;
        }

        return Math.min((rating / 1800.0) * 80.0 + (Math.min(contests, 10) / 10.0) * 20.0, 100.0);
    }

    // GitHub Score Calculation
    public double calculateGithubScore(GithubStats stats) {
        if (stats == null) {
            return 0.0;
        }
        int repos = stats.getPublicRepos() != null ? stats.getPublicRepos() : 0;
        int stars = stats.getStars() != null ? stats.getStars() : 0;
        int followers = stats.getFollowers() != null ? stats.getFollowers() : 0;

        double repoContribution = (Math.min(repos, 15) / 15.0) * 50.0;
        double starContribution = (Math.min(stars, 25) / 25.0) * 30.0;
        double followerContribution = (Math.min(followers, 20) / 20.0) * 20.0;

        return Math.min(repoContribution + starContribution + followerContribution, 100.0);
    }

    // Developer Score Calculation
    public double calculateDeveloperScore(double dsa, double github, double contest) {
        return (0.5 * dsa) + (0.3 * github) + (0.2 * contest);
    }

    // Readiness Classification
    public String getReadinessClassification(double score) {
        if (score < 40.0) {
            return "Beginner";
        } else if (score < 75.0) {
            return "Intermediate";
        } else if (score < 90.0) {
            return "Placement Ready";
        } else {
            return "Strong Candidate";
        }
    }

    // Weakness Detection
    public List<String> generateWeaknesses(LeetcodeStats leetcodeStats, GithubStats githubStats) {
        List<String> weaknesses = new ArrayList<>();

        int hardSolved = (leetcodeStats != null && leetcodeStats.getHardSolved() != null) ? leetcodeStats.getHardSolved() : 0;
        if (hardSolved < 20) {
            weaknesses.add("Practice harder problems.");
            weaknesses.add("Solve more hard problems");
        }

        int publicRepos = (githubStats != null && githubStats.getPublicRepos() != null) ? githubStats.getPublicRepos() : 0;
        if (publicRepos < 5) {
            weaknesses.add("Build more public projects.");
        }

        double contestRating = (leetcodeStats != null && leetcodeStats.getContestRating() != null) ? leetcodeStats.getContestRating() : 0.0;
        int contestsAttended = (leetcodeStats != null && leetcodeStats.getContestsAttended() != null) ? leetcodeStats.getContestsAttended() : 0;
        if (contestRating < 1500) {
            weaknesses.add("Participate in more contests.");
        }
        if (contestsAttended < 5) {
            weaknesses.add("Improve contest participation");
        }

        return weaknesses;
    }

    // Strength Detection
    public List<String> generateStrengths(LeetcodeStats leetcodeStats, GithubStats githubStats) {
        List<String> strengths = new ArrayList<>();

        int hardSolved = (leetcodeStats != null && leetcodeStats.getHardSolved() != null) ? leetcodeStats.getHardSolved() : 0;
        if (hardSolved > 100) {
            strengths.add("Strong problem solving ability");
        }

        int stars = (githubStats != null && githubStats.getStars() != null) ? githubStats.getStars() : 0;
        if (stars > 200) {
            strengths.add("Strong open-source profile");
        }

        double contestRating = (leetcodeStats != null && leetcodeStats.getContestRating() != null) ? leetcodeStats.getContestRating() : 0.0;
        if (contestRating > 1800) {
            strengths.add("Excellent competitive programming skills");
        }

        int publicRepos = (githubStats != null && githubStats.getPublicRepos() != null) ? githubStats.getPublicRepos() : 0;
        if (publicRepos >= 10) {
            strengths.add("Good project portfolio");
        }

        return strengths;
    }
}
