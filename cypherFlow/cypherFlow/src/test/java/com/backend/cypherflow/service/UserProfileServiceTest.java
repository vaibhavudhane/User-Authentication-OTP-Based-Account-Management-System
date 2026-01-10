package com.backend.cypherflow.service;

import com.backend.cypherflow.dto.request.RegisterRequest;
import com.backend.cypherflow.dto.request.UpdateProfileRequest;
import com.backend.cypherflow.dto.response.UserProfileResponse;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.entity.UserProfile;
import com.backend.cypherflow.enums.Gender;
import com.backend.cypherflow.exception.InvalidContentException;
import com.backend.cypherflow.exception.ResourceNotFoundException;
import com.backend.cypherflow.repository.UserProfileRepository;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    // ---------- HELPERS ----------

    private User validUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("vaibhav");
        return user;
    }

    private UserProfile validProfile() {
        UserProfile profile = new UserProfile();
        profile.setId(10L);
        profile.setUser(validUser());
        profile.setFullName("Vaibhav Udhane");
        return profile;
    }

    // ================= CREATE PROFILE =================

    @Test
    void createProfileForUser_shouldSaveProfile() {

        User user = validUser();

        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Vaibhav");
        req.setLastName("Udhane");
        req.setGender(Gender.MALE);
        req.setAddress("Pune");

        userProfileService.createProfileForUser(user, req);

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    // ================= GET PROFILE =================

    @Test
    void getProfile_shouldReturnProfile_whenExists() {

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.of(validProfile()));

            UserProfileResponse response = userProfileService.getProfile();

            assertEquals("Vaibhav Udhane", response.getFullName());
        }
    }

    @Test
    void getProfile_shouldThrowException_whenProfileNotFound() {

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userProfileService.getProfile());
        }
    }

    // ================= UPDATE PROFILE =================

    @Test
    void updateProfile_shouldUpdateOnlyProvidedFields() {

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            UserProfile profile = validProfile();

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.of(profile));

            UpdateProfileRequest req = new UpdateProfileRequest();
            req.setFullName("New Name");
            req.setBio("Java Developer");

            UserProfileResponse response = userProfileService.updateProfile(req);

            assertEquals("New Name", response.getFullName());
            assertEquals("Java Developer", response.getBio());

            verify(userProfileRepository).save(profile);
        }
    }

    @Test
    void updateProfile_shouldThrowException_whenProfileNotFound() {

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userProfileService.updateProfile(new UpdateProfileRequest()));
        }
    }

    // ================= UPLOAD PROFILE PICTURE =================

    @Test
    void uploadProfilePicture_shouldThrowException_whenFileTooLarge() {

        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(11 * 1024 * 1024L);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.of(validProfile()));

            assertThrows(MaxUploadSizeExceededException.class,
                    () -> userProfileService.uploadProfilePicture(file));
        }
    }

    @Test
    void uploadProfilePicture_shouldThrowException_whenInvalidContentType() {

        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.of(validProfile()));

            assertThrows(InvalidContentException.class,
                    () -> userProfileService.uploadProfilePicture(file));
        }
    }

    @Test
    void uploadProfilePicture_shouldThrowException_whenProfileNotFound() {

        MultipartFile file = mock(MultipartFile.class);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            when(userProfileRepository.findByUserId(1L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userProfileService.uploadProfilePicture(file));
        }
    }
}

