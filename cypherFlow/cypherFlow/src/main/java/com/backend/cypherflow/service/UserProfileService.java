package com.backend.cypherflow.service;

import com.backend.cypherflow.dto.request.RegisterRequest;
import com.backend.cypherflow.dto.request.UpdateProfileRequest;
import com.backend.cypherflow.dto.response.UserProfileResponse;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.entity.UserProfile;
import com.backend.cypherflow.exception.InvalidContentException;
import com.backend.cypherflow.exception.ResourceNotFoundException;
import com.backend.cypherflow.repository.UserProfileRepository;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import static com.backend.cypherflow.util.SecurityUtil.getCurrentUserId;
@Service
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository)
    {
        this.userProfileRepository=userProfileRepository;
        this.userRepository=userRepository;
    }


    /* ================= CREATE PROFILE ================= */

    public void createProfileForUser(User user, RegisterRequest request) {

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFirstName() + " " + request.getLastName())
                .gender(request.getGender())
                .dob(request.getDob())
                .address(request.getAddress())
                .build();


        userProfileRepository.save(profile);

        log.info(
                "User profile created successfully. userId={}",
                user.getId()
        );
    }


    /* ================= GET PROFILE ================= */

    public UserProfileResponse getProfile() {

        //Get current logged-in userId (JWT based)
        Long userId = getCurrentUserId();

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found"));

        if (log.isDebugEnabled()) {
            log.debug("Profile fetched. userId={}", userId);
        }

        return UserProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .website(profile.getWebsite())
                .location(profile.getAddress())
                .gender(profile.getGender())
                .dob(profile.getDob())
                .build();
    }

    /* ================= UPDATE PROFILE ================= */

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (log.isDebugEnabled()) {
            log.debug("Profile update requested. userId={}", userId);
        }

        // Update ONLY what is present
        if (request.getFullName() != null)
            profile.setFullName(request.getFullName());

        if (request.getBio() != null)
            profile.setBio(request.getBio());

        if (request.getWebsite() != null)
            profile.setWebsite(request.getWebsite());

        if (request.getAddress() != null)
            profile.setAddress(request.getAddress());

        if (request.getGender() != null)
            profile.setGender(request.getGender());

        if (request.getDob() != null)
            profile.setDob(request.getDob());

        userProfileRepository.save(profile);
        log.info("Profile updated successfully. userId={}", userId);

        // return response directly
        return UserProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .website(profile.getWebsite())
                .location(profile.getAddress())
                .gender(profile.getGender())
                .dob(profile.getDob())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .build();


    }


    /* ================= UPLOAD PROFILE PICTURE ================= */

    public String uploadProfilePicture(MultipartFile file) {

        Long userId = SecurityUtil.getCurrentUserId();

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        //  Folder path
        String uploadDir = "C:\\Users\\delll\\Pictures";

        if (file.getSize() > 10 * 1024 * 1024) {
            log.warn("Profile picture upload rejected - file too large. userId={}", userId);
            throw new MaxUploadSizeExceededException(10);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Profile picture upload rejected - invalid content type. userId={}", userId);
            throw new InvalidContentException("Only image files are allowed");
        }


        // Create folder if not exists

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //  Create unique file name
        String fileName = userId + "_" + file.getOriginalFilename();

        //  Save file
        File destination = new File(uploadDir + fileName);

        try {
            file.transferTo(destination);
        }
        catch (IOException e)
        {
            log.error("Profile picture upload failed. userId={}", userId, e);
            throw new RuntimeException("Profile picture upload failed");
        }

        // Save file path in DB
        profile.setProfilePictureUrl(destination.getAbsolutePath());
        userProfileRepository.save(profile);
        log.info("Profile picture uploaded successfully. userId={}", userId);

        // Return path
        return destination.getAbsolutePath();
    }


}


