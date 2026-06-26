package com.coderank.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private Double developerScore;
    private Double dsaScore;
    private Double githubScore;
    private Double contestScore;
    private String classification;
    private List<String> strengths;
    private List<String> weaknesses;
}
