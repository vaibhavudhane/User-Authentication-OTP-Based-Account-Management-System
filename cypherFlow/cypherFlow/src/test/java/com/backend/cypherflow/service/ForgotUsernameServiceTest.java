package com.backend.cypherflow.service;

import com.backend.cypherflow.util.AccountStatusValidator;
import com.backend.cypherflow.dto.request.ForgotUsernameRequest;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.exception.UserNotFoundException;
import com.backend.cypherflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotUsernameServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AccountStatusValidator accountStatusValidator;

    @InjectMocks
    private ForgotUsernameService forgotUsernameService;

    // ---------- TEST HELPERS ----------

    private ForgotUsernameRequest validRequest() {
        ForgotUsernameRequest req = new ForgotUsernameRequest();
        req.setEmail("test@gmail.com");
        return req;
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setUsername("vaibhav");
        return user;
    }

    // ---------- TC-01: USER NOT FOUND ----------

    @Test
    void forgotUsername_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> forgotUsernameService.forgotUsername(validRequest()));

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(accountStatusValidator, never()).validate(any());
    }

    // ---------- TC-02: ACCOUNT STATUS INVALID ----------

    @Test
    void forgotUsername_shouldFail_whenAccountStatusInvalid() {

        User user = validUser();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException("Account blocked"))
                .when(accountStatusValidator).validate(user);

        assertThrows(RuntimeException.class,
                () -> forgotUsernameService.forgotUsername(validRequest()));

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ---------- TC-03: SUCCESSFUL USERNAME RECOVERY ----------

    @Test
    void forgotUsername_shouldSendEmail_whenUserIsValid() {

        User user = validUser();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doNothing().when(accountStatusValidator).validate(user);

        forgotUsernameService.forgotUsername(validRequest());

        verify(accountStatusValidator).validate(user);

        verify(emailService).sendEmail(
                eq("test@gmail.com"),
                eq("Username Recovery"),
                contains("vaibhav")
        );
    }
}

