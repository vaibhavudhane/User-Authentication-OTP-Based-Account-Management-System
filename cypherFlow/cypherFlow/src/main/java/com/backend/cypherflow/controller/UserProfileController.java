package com.backend.cypherflow.controller;

import com.backend.cypherflow.dto.request.UpdateProfileRequest;
import com.backend.cypherflow.dto.response.UserProfileResponse;
import com.backend.cypherflow.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService)
    {
        this.userProfileService=userProfileService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public UserProfileResponse getMyProfile()
    {
        return userProfileService.getProfile();
    }

    @PutMapping("/update")
    public UserProfileResponse updateMyProfile(@Valid @RequestBody UpdateProfileRequest request)
    {
        return userProfileService.updateProfile(request);
    }


    @PostMapping("/uploadProfilePhoto")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file)
    {
        userProfileService.uploadProfilePicture(file);
        return ResponseEntity.ok("profile photo uploaded successfully");
    }


}

