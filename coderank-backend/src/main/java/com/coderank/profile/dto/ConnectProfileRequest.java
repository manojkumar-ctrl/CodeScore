package com.coderank.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectProfileRequest {

    @NotBlank(message = "GitHub username is required")
    private String githubUsername;

    @NotBlank(message = "LeetCode username is required")
    private String leetcodeUsername;

    private String codeforcesHandle;
}
