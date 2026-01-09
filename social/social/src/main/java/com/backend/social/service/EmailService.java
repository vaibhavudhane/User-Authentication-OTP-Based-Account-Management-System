package com.backend.social.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender)
    {
        this.mailSender=mailSender;
    }

    /* ================= GENERIC EMAIL ================= */

    public void sendEmail(String to, String subject, String body) {

        try {

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);

            if (log.isDebugEnabled()) {
                log.debug("Email sent successfully. subject={}", subject);
            }
        } catch (Exception e) {
            log.error(
                    "Email sending failed. subject={}",
                    subject,
                    e
            );
            throw e;
        }
    }

    /* ================= PASSWORD RESET EMAIL ================= */

    @Async
    public void sendPasswordResetEmail(String email, String resetLink) {

        if (log.isDebugEnabled()) {
            log.debug("Initiating password reset email dispatch");
        }

        String subject = "Reset Your Password";

        String body = """
                Hello,

                We received a request to reset your password.

                Click the link below to reset your password:
                %s

                This link is valid for 30 minutes and can be used only once.

                If you did not request a password reset, please ignore this email.
                Your account is safe.

                Regards,
                Support Team
                """.formatted(resetLink);

        sendEmail(email, subject, body);

    }

    /* ================= PASSWORD RESET ALERT ================= */

    @Async
    public void sendPasswordResetAlertEmail(String email, String blockLink) {

        if (log.isDebugEnabled()) {
            log.debug("Initiating password reset alert email dispatch");
        }

        String subject = "Security Alert: Password Changed";

        String body =
                "Hello,\n\n" +
                        "Your account password was recently reset.\n\n" +
                        "If YOU performed this action, no further steps are needed.\n\n" +
                        "If this was NOT you, immediately block your account using the link below:\n\n" +
                        blockLink + "\n\n" +
                        "This link is valid for 1 hour and can be used only once.\n\n" +
                        "Regards,\nSecurity Team";

        sendEmail(email, subject, body);
    }

    /* ================= ACCOUNT UNBLOCKED ================= */


    public void sendAccountUnblockedEmail(String email) {

        if (log.isDebugEnabled()) {
            log.debug("Initiating account unblocked email dispatch");
        }

        String subject = "Your Account Has Been Unblocked";

        String body =
                "Hello,\n\n" +
                        "Your account has been successfully unblocked.\n\n" +
                        "You can now log in using your credentials.\n\n" +
                        "If you did NOT perform this action, please contact our support team immediately.\n\n" +
                        "Regards,\n" +
                        "Security Team";

        sendEmail(email, subject, body);
    }

}

