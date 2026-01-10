package com.backend.cypherflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    // ---------- TC-01: BASIC EMAIL ----------

    @Test
    void sendEmail_shouldSendMail() {

        emailService.sendEmail(
                "test@gmail.com",
                "Test Subject",
                "Test Body"
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertEquals("test@gmail.com", msg.getTo()[0]);
        assertEquals("Test Subject", msg.getSubject());
        assertEquals("Test Body", msg.getText());
    }

    // ---------- TC-02: PASSWORD RESET EMAIL ----------

    @Test
    void sendPasswordResetEmail_shouldSendResetMail() {

        emailService.sendPasswordResetEmail(
                "user@gmail.com",
                "http://reset-link"
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ---------- TC-03: PASSWORD RESET ALERT ----------

    @Test
    void sendPasswordResetAlertEmail_shouldSendAlertMail() {

        emailService.sendPasswordResetAlertEmail(
                "user@gmail.com",
                "http://block-link"
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ---------- TC-04: ACCOUNT UNBLOCKED EMAIL ----------

    @Test
    void sendAccountUnblockedEmail_shouldSendUnblockedMail() {

        emailService.sendAccountUnblockedEmail("user@gmail.com");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
