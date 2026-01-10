package com.backend.cypherflow.service;

import com.backend.cypherflow.dto.request.RegisterRequest;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.AccountStatus;
import com.backend.cypherflow.enums.Role;
import com.backend.cypherflow.exception.BadRequestException;
import com.backend.cypherflow.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {


    //mock dependencies
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private OtpService otpService;

    @Mock
    private UserProfileService userProfileService;

    // ---------- CLASS UNDER TEST ----------

    @InjectMocks
    private RegistrationService registrationService;



    // test helper
    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Vaibhav");
        request.setEmail("vaibhav@gmail.com");
        request.setMobile("9999999999");
        request.setPassword("password");
        return request;
    }


    // ---------- TC-01: NEW USER (HAPPY PATH) ----------

    @Test
    void registerOrReuse_shouldCreateNewUser_whenUserDoesNotExist() {

        // Arrange
        RegisterRequest request = validRequest();

        when(userRepository.findByEmailIgnoreCase("vaibhav@gmail.com"))
                .thenReturn(Optional.empty());

        when(encoder.encode("password"))
                .thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username("vaibhav")
                .email("vaibhav@gmail.com")
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        Long userId = registrationService.registerOrReuse(request);

        // Assert
        assertEquals(1L, userId);

        verify(userRepository).findByEmailIgnoreCase("vaibhav@gmail.com");
        verify(encoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(userProfileService).createProfileForUser(savedUser, request);
    }

    // ---------- TC-02: REUSE UNVERIFIED USER ----------

    @Test
    void registerOrReuse_shouldReuseUser_whenPendingVerification() {

        // Arrange
        RegisterRequest request = validRequest();

        User existingUser = User.builder()
                .id(5L)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        when(userRepository.findByEmailIgnoreCase("vaibhav@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Act
        Long userId = registrationService.registerOrReuse(request);

        // Assert
        assertEquals(5L, userId);

        verify(userRepository, never()).save(any());
        verify(userProfileService, never()).createProfileForUser(any(), any());
    }

    // ---------- TC-03: USER ALREADY EXISTS ----------

    @Test
    void registerOrReuse_shouldThrowException_whenUserAlreadyExists() {

        // Arrange
        RegisterRequest request = validRequest();

        User existingUser = User.builder()
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByEmailIgnoreCase("vaibhav@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Act + Assert
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerOrReuse(request)
        );

        assertEquals("User already exists", ex.getMessage());
    }

    // ---------- TC-04: EMAIL UNIQUE CONSTRAINT ----------

    @Test
    void registerOrReuse_shouldThrowException_whenEmailConstraintViolated() {

        RegisterRequest request = validRequest();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        ConstraintViolationException cve =
                new ConstraintViolationException("email", null, "uk_users_email");

        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("dup", cve));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerOrReuse(request)
        );

        assertEquals("Email already registered", ex.getMessage());
    }

    // ---------- TC-05: USERNAME UNIQUE CONSTRAINT ----------

    @Test
    void registerOrReuse_shouldThrowException_whenUsernameConstraintViolated() {

        RegisterRequest request = validRequest();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        ConstraintViolationException cve =
                new ConstraintViolationException("username", null, "uk_users_username");

        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("dup", cve));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerOrReuse(request)
        );

        assertEquals("Username already exists", ex.getMessage());
    }

    // ---------- TC-06: MOBILE UNIQUE CONSTRAINT ----------

    @Test
    void registerOrReuse_shouldThrowException_whenMobileConstraintViolated() {

        RegisterRequest request = validRequest();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        ConstraintViolationException cve =
                new ConstraintViolationException("mobile", null, "uk_users_mobile");

        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("dup", cve));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerOrReuse(request)
        );

        assertEquals("Mobile number already exists", ex.getMessage());
    }

    // ---------- TC-07: UNKNOWN CONSTRAINT ----------

    @Test
    void registerOrReuse_shouldThrowGenericException_whenUnknownConstraint() {

        RegisterRequest request = validRequest();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        ConstraintViolationException cve =
                new ConstraintViolationException("unknown", null, "random_constraint");

        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("err", cve));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerOrReuse(request)
        );

        assertEquals("User already exists", ex.getMessage());
    }
}

