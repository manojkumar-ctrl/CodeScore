package com.coderank.profile.entity;

import com.coderank.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "codeforces_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeforcesStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer rating;
    private Integer maxRating;
    private String rankName; // Note: 'rank' is often a reserved keyword in SQL, using rankName
    private String maxRank;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
