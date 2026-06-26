package com.coderank.profile.repository;

import com.coderank.profile.entity.LeetcodeStats;
import com.coderank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeetcodeStatsRepository extends JpaRepository<LeetcodeStats, Long> {
    Optional<LeetcodeStats> findByUser(User user);
}
