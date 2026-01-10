package com.backend.cypherflow.service;

import com.backend.cypherflow.util.AccountStatusValidator;
import com.backend.cypherflow.dto.request.ForgotUsernameRequest;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.exception.UserNotFoundException;
import com.backend.cypherflow.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForgotUsernameService {


    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AccountStatusValidator accountStatusValidator;

    public ForgotUsernameService(UserRepository userRepository,
                                 EmailService emailService,
                                 AccountStatusValidator accountStatusValidator)
    {
        this.userRepository=userRepository;
        this.emailService=emailService;
        this.accountStatusValidator=accountStatusValidator;
    }

    public void forgotUsername(ForgotUsernameRequest req) {

        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        accountStatusValidator.validate(user);

        // Security email
        emailService.sendEmail(
                user.getEmail(),
                "Username Recovery",
                "Your username is : "+user.getUsername()
        );

        if (log.isDebugEnabled()) {
            log.debug("Username recovery email dispatched successfully");
        }
    }


}
