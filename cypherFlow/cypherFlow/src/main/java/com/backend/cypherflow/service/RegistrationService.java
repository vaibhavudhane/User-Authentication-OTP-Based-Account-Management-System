package com.backend.cypherflow.service;

import com.backend.cypherflow.dto.request.RegisterRequest;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.AccountStatus;
import com.backend.cypherflow.enums.Role;
import com.backend.cypherflow.exception.BadRequestException;
import com.backend.cypherflow.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Slf4j
public class RegistrationService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final UserProfileService userProfileService;

    public RegistrationService(UserProfileService userProfileService,
                               OtpService otpService,
                               UserRepository userRepository,
                               PasswordEncoder encoder)
    {
        this.userProfileService = userProfileService;
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }


    @Transactional
    public Long registerOrReuse(RegisterRequest request) {


        if (log.isDebugEnabled()) {
            log.debug("Registration request received");
        }


        // IDEMPOTENT BEHAVIOR
        Optional<User> existingOpt = userRepository.findByEmailIgnoreCase(request.getEmail());

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            if (existing.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
                log.info(
                        "Reusing existing unverified account. userId={}",
                        existing.getId()
                );
                return existing.getId(); // reuse
            }

            log.warn(
                    "Registration blocked - user already exists. userId={}",
                    existing.getId()
            );
            throw new BadRequestException("User already exists");
        }


        // CREATE NEW USER
        User user = User.builder()
                .username(request.getUsername().toLowerCase())
                .email(request.getEmail().toLowerCase())
                .mobile(request.getMobile())
                .passwordHash(encoder.encode(request.getPassword()))
                .emailVerified(false)
                .mobileVerified(false)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .role(Role.USER)
                .build();

            User savedUser = saveUserSafely(user);
            userProfileService.createProfileForUser(savedUser, request);

            log.info(
                    "User registration completed successfully. userId={}",
                    savedUser.getId()
            );
            return savedUser.getId();

    }

    private User saveUserSafely(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.warn("User registration constraint violation");
            throw mapConstraintViolation(ex);
        }
    }
    private BadRequestException mapConstraintViolation(
            DataIntegrityViolationException ex) {

        Throwable root = ex.getMostSpecificCause();

        if (root instanceof org.hibernate.exception.ConstraintViolationException cve) {
            return switch (cve.getConstraintName()) {
                case "uk_users_email" -> new BadRequestException("Email already registered");
                case "uk_users_username" -> new BadRequestException("Username already exists");
                case "uk_users_mobile" -> new BadRequestException("Mobile number already exists");
                default -> new BadRequestException("User already exists");
            };
        }

        return new BadRequestException("User already exists");
    }


}
