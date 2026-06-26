package com.coderank.analysis.repository;

import com.coderank.analysis.entity.AnalysisResult;
import com.coderank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Get the latest analysis result per user, ordered by developer score descending.
     * Uses a subquery to pick only the most recent result for each user.
     */
    @Query("""
        SELECT a FROM AnalysisResult a
        WHERE a.createdAt = (
            SELECT MAX(a2.createdAt) FROM AnalysisResult a2 WHERE a2.user = a.user
        )
        ORDER BY a.developerScore DESC
    """)
    List<AnalysisResult> findLatestPerUserOrderByDeveloperScoreDesc();

    @Query("""
        SELECT a FROM AnalysisResult a
        WHERE a.createdAt = (
            SELECT MAX(a2.createdAt) FROM AnalysisResult a2 WHERE a2.user = a.user
        )
        ORDER BY a.dsaScore DESC
    """)
    List<AnalysisResult> findLatestPerUserOrderByDsaScoreDesc();

    @Query("""
        SELECT a FROM AnalysisResult a
        WHERE a.createdAt = (
            SELECT MAX(a2.createdAt) FROM AnalysisResult a2 WHERE a2.user = a.user
        )
        ORDER BY a.githubScore DESC
    """)
    List<AnalysisResult> findLatestPerUserOrderByGithubScoreDesc();

    @Query("""
        SELECT a FROM AnalysisResult a
        WHERE a.createdAt = (
            SELECT MAX(a2.createdAt) FROM AnalysisResult a2 WHERE a2.user = a.user
        )
        ORDER BY a.contestScore DESC
    """)
    List<AnalysisResult> findLatestPerUserOrderByContestScoreDesc();
}
