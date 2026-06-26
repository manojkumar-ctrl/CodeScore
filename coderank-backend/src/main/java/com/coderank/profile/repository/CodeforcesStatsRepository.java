package com.coderank.profile.repository;

import com.coderank.profile.entity.CodeforcesStats;
import com.coderank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeforcesStatsRepository extends JpaRepository<CodeforcesStats, Long> {
    Optional<CodeforcesStats> findByUser(User user);
}
