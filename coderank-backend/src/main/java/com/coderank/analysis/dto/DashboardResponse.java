package com.coderank.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Double developerScore;
    private Integer leetcodeSolved;
    private Double contestRating;
    private Integer githubRepos;
    private String classification;
    
    // New detailed stats
    private Integer githubCommits;
    private Integer githubStars;
    private Integer githubFollowers;
    private Integer leetcodeEasy;
    private Integer leetcodeMedium;
    private Integer leetcodeHard;
    private Integer leetcodeContests;
}
