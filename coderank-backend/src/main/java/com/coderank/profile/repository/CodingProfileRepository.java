package com.coderank.profile.repository;

import com.coderank.profile.entity.CodingProfile;
import com.coderank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodingProfileRepository extends JpaRepository<CodingProfile, Long> {
    Optional<CodingProfile> findByUser(User user);
}
