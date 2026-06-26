package com.coderank.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareResponse {

    private ScoreData me;
    private ScoreData friend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreData {
        private Double developerScore;
        private Double dsaScore;
        private Double githubScore;
        private Double contestScore;
        private String classification;
        private Integer leetcodeSolved;
        private Double contestRating;
        private Integer githubRepos;
    }
}
