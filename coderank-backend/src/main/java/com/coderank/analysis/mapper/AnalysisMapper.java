package com.coderank.analysis.mapper;

import com.coderank.analysis.dto.AnalysisResponse;
import com.coderank.analysis.dto.DashboardResponse;
import com.coderank.analysis.entity.AnalysisResult;
import com.coderank.profile.entity.GithubStats;
import com.coderank.profile.entity.LeetcodeStats;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalysisMapper {

    public AnalysisResponse toAnalysisResponse(
            AnalysisResult result,
            String classification,
            List<String> strengths,
            List<String> weaknesses) {
        if (result == null) {
            return null;
        }

        return AnalysisResponse.builder()
                .developerScore(result.getDeveloperScore())
                .dsaScore(result.getDsaScore())
                .githubScore(result.getGithubScore())
                .contestScore(result.getContestScore())
                .classification(classification)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .build();
    }

    public DashboardResponse toDashboardResponse(
            AnalysisResult result,
            LeetcodeStats leetcodeStats,
            GithubStats githubStats,
            String classification) {
        
        Integer leetcodeSolved = (leetcodeStats != null && leetcodeStats.getTotalSolved() != null) ? leetcodeStats.getTotalSolved() : 0;
        Double contestRating = (leetcodeStats != null && leetcodeStats.getContestRating() != null) ? leetcodeStats.getContestRating() : 0.0;
        Integer githubRepos = (githubStats != null && githubStats.getPublicRepos() != null) ? githubStats.getPublicRepos() : 0;

        Double devScore = result != null ? result.getDeveloperScore() : 0.0;

        return DashboardResponse.builder()
                .developerScore(devScore)
                .leetcodeSolved(leetcodeSolved)
                .contestRating(contestRating)
                .githubRepos(githubRepos)
                .classification(classification)
                .githubCommits((githubStats != null && githubStats.getTotalCommits() != null) ? githubStats.getTotalCommits() : 0)
                .githubStars((githubStats != null && githubStats.getStars() != null) ? githubStats.getStars() : 0)
                .githubFollowers((githubStats != null && githubStats.getFollowers() != null) ? githubStats.getFollowers() : 0)
                .leetcodeEasy((leetcodeStats != null && leetcodeStats.getEasySolved() != null) ? leetcodeStats.getEasySolved() : 0)
                .leetcodeMedium((leetcodeStats != null && leetcodeStats.getMediumSolved() != null) ? leetcodeStats.getMediumSolved() : 0)
                .leetcodeHard((leetcodeStats != null && leetcodeStats.getHardSolved() != null) ? leetcodeStats.getHardSolved() : 0)
                .leetcodeContests((leetcodeStats != null && leetcodeStats.getContestsAttended() != null) ? leetcodeStats.getContestsAttended() : 0)
                .build();
    }
}
