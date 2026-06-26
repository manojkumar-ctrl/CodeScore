package com.coderank.analysis.controller;

import com.coderank.analysis.dto.AnalysisResponse;
import com.coderank.analysis.dto.CompareResponse;
import com.coderank.analysis.dto.DashboardResponse;
import com.coderank.analysis.dto.LeaderboardEntry;
import com.coderank.analysis.service.AnalysisService;
import com.coderank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/run")
    public ResponseEntity<AnalysisResponse> runAnalysis(@AuthenticationPrincipal User user) {
        AnalysisResponse response = analysisService.runAnalysis(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the latest analysis result without re-running.
     */
    @GetMapping("/latest")
    public ResponseEntity<AnalysisResponse> getLatestAnalysis(@AuthenticationPrincipal User user) {
        AnalysisResponse response = analysisService.getLatestAnalysis(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        DashboardResponse response = analysisService.getDashboard(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get leaderboard entries ranked by category (overall, dsa, github, contest).
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "overall") String category) {
        List<LeaderboardEntry> entries = analysisService.getLeaderboard(category);
        return ResponseEntity.ok(entries);
    }

    /**
     * Compare current user's analysis with another user's by name or email.
     */
    @GetMapping("/compare")
    public ResponseEntity<CompareResponse> compareWith(
            @AuthenticationPrincipal User user,
            @RequestParam String username) {
        CompareResponse response = analysisService.compareWith(user, username);
        return ResponseEntity.ok(response);
    }
}
