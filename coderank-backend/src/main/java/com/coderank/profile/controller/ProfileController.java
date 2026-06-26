package com.coderank.profile.controller;

import com.coderank.profile.dto.ConnectProfileRequest;
import com.coderank.profile.dto.ConnectProfileResponse;
import com.coderank.profile.dto.ProfileResponse;
import com.coderank.profile.service.ProfileService;
import com.coderank.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/connect")
    public ResponseEntity<ConnectProfileResponse> connectProfiles(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ConnectProfileRequest request) {
        
        ConnectProfileResponse response = profileService.connectProfiles(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfiles(@AuthenticationPrincipal User user) {
        ProfileResponse response = profileService.getProfiles(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ConnectProfileResponse> updateProfiles(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ConnectProfileRequest request) {
        
        // Update is practically the same as connect logic for validation and saving
        ConnectProfileResponse response = profileService.connectProfiles(user, request);
        return ResponseEntity.ok(response);
    }
}
