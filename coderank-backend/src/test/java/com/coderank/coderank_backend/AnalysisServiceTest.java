package com.coderank.coderank_backend;

import com.coderank.analysis.service.AnalysisService;
import com.coderank.profile.entity.GithubStats;
import com.coderank.profile.entity.LeetcodeStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceTest {

    private AnalysisService service;

    @BeforeEach
    void setUp() {
        // Construct the service manually for unit testing calculations without loading database context
        service = new AnalysisService(null, null, null, null, null, null);
    }

    @Test
    void testDsaScoreCalculation() {
        // Example: Easy = 150, Medium = 200, Hard = 50
        // Raw score: 150*1 + 200*2 + 50*4 = 750
        // Normalization: (750 / 1500) * 100 = 50.0
        LeetcodeStats stats = LeetcodeStats.builder()
                .easySolved(150)
                .mediumSolved(200)
                .hardSolved(50)
                .build();

        double dsaScore = service.calculateDsaScore(stats);
        assertEquals(50.0, dsaScore, 0.001);

        // Test max cap of 100
        LeetcodeStats highStats = LeetcodeStats.builder()
                .easySolved(1000)
                .mediumSolved(500)
                .hardSolved(200)
                .build();
        double highDsaScore = service.calculateDsaScore(highStats);
        assertEquals(100.0, highDsaScore, 0.001);

        // Test null safety
        double nullDsaScore = service.calculateDsaScore(null);
        assertEquals(0.0, nullDsaScore, 0.001);
    }

    @Test
    void testContestScoreCalculation() {
        // Example: rating = 1800, contests = 15
        // (1800 / 2500) * 80 + (15 / 20) * 20 = 57.6 + 15.0 = 72.6
        LeetcodeStats stats = LeetcodeStats.builder()
                .contestRating(1800.0)
                .contestsAttended(15)
                .build();

        double contestScore = service.calculateContestScore(stats);
        assertEquals(72.6, contestScore, 0.001);

        // Example: no contests
        LeetcodeStats noContests = LeetcodeStats.builder()
                .contestRating(0.0)
                .contestsAttended(0)
                .build();
        double noContestScore = service.calculateContestScore(noContests);
        assertEquals(0.0, noContestScore, 0.001);

        // Example: null stats
        double nullContestScore = service.calculateContestScore(null);
        assertEquals(0.0, nullContestScore, 0.001);
    }

    @Test
    void testGithubScoreCalculation() {
        // Example: repos = 15, stars = 50, followers = 20
        // repoContribution = min(15,30)/30 * 50 = 25
        // starContribution = min(50,100)/100 * 30 = 15
        // followerContribution = min(20,100)/100 * 20 = 4
        // Total = 44
        GithubStats stats = GithubStats.builder()
                .publicRepos(15)
                .stars(50)
                .followers(20)
                .build();

        double githubScore = service.calculateGithubScore(stats);
        assertEquals(44.0, githubScore, 0.001);

        // Test null stats
        double nullGithubScore = service.calculateGithubScore(null);
        assertEquals(0.0, nullGithubScore, 0.001);
    }

    @Test
    void testDeveloperScoreAndReadinessClassification() {
        // Example: DSA = 85, GitHub = 60, Contest = 70
        // developerScore = 0.5*85 + 0.3*60 + 0.2*70 = 42.5 + 18 + 14 = 74.5
        double devScore = service.calculateDeveloperScore(85.0, 60.0, 70.0);
        assertEquals(74.5, devScore, 0.001);

        // Test classifications
        // Beginner: < 40
        assertEquals("Beginner", service.getReadinessClassification(39.9));
        // Intermediate: 40 <= score < 70
        assertEquals("Intermediate", service.getReadinessClassification(40.0));
        assertEquals("Intermediate", service.getReadinessClassification(69.9));
        // Placement Ready: 70 <= score < 85
        assertEquals("Placement Ready", service.getReadinessClassification(70.0));
        assertEquals("Placement Ready", service.getReadinessClassification(84.9));
        // Strong Candidate: >= 85
        assertEquals("Strong Candidate", service.getReadinessClassification(85.0));
    }

    @Test
    void testWeaknessAndStrengthDetection() {
        LeetcodeStats leetcodeStats = LeetcodeStats.builder()
                .hardSolved(15) // < 20 (Weak DSA)
                .contestRating(1400.0) // < 1500 (Weak Contest Performance)
                .contestsAttended(3) // < 5 (Improve contest participation)
                .build();

        GithubStats githubStats = GithubStats.builder()
                .publicRepos(4) // < 5 (Weak Dev Profile)
                .stars(5)
                .build();

        List<String> weaknesses = service.generateWeaknesses(leetcodeStats, githubStats);
        assertTrue(weaknesses.contains("Practice harder problems."));
        assertTrue(weaknesses.contains("Solve more hard problems"));
        assertTrue(weaknesses.contains("Build more public projects."));
        assertTrue(weaknesses.contains("Participate in more contests."));
        assertTrue(weaknesses.contains("Improve contest participation"));

        // Test strengths
        LeetcodeStats strongLeetcode = LeetcodeStats.builder()
                .hardSolved(120) // > 100 (Strong problem solving)
                .contestRating(1900.0) // > 1800 (Excellent CP)
                .build();

        GithubStats strongGithub = GithubStats.builder()
                .publicRepos(12) // >= 10 (Good project portfolio)
                .stars(250) // > 200 (Strong open-source profile)
                .build();

        List<String> strengths = service.generateStrengths(strongLeetcode, strongGithub);
        assertTrue(strengths.contains("Strong problem solving ability"));
        assertTrue(strengths.contains("Strong open-source profile"));
        assertTrue(strengths.contains("Excellent competitive programming skills"));
        assertTrue(strengths.contains("Good project portfolio"));
    }
}
