package com.coderank.profile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectProfileResponse {
    private String message;
    private boolean profileConnected;
    private boolean githubVerified;
    private boolean leetcodeVerified;
    private Boolean codeforcesVerified; // Nullable if handle not provided
}
