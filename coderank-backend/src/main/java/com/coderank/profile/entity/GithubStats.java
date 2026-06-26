package com.coderank.profile.entity;

import com.coderank.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer followers;
    private Integer following;
    private Integer publicRepos;
    private Integer stars;
    private Integer totalCommits;

    @Column(columnDefinition = "TEXT")
    private String languages; // Can store as JSON string or comma-separated

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
