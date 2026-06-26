package com.coderank.profile.repository;

import com.coderank.profile.entity.GithubStats;
import com.coderank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubStatsRepository extends JpaRepository<GithubStats, Long> {
    Optional<GithubStats> findByUser(User user);
}
