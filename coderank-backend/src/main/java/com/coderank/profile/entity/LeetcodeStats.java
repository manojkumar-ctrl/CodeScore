package com.coderank.profile.entity;

import com.coderank.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "leetcode_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeetcodeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leetcodeUsername;

    private Integer totalSolved;

    private Integer easySolved;

    private Integer mediumSolved;

    private Integer hardSolved;

    private Double contestRating;

    private Integer contestGlobalRanking;

    private Integer contestsAttended;

    private Integer ranking;

    private Integer reputation;

    private LocalDateTime lastSyncedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
}
