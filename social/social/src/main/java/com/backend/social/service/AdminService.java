package com.backend.social.service;

import com.backend.social.entity.User;
import com.backend.social.exception.ResourceNotFoundException;
import com.backend.social.exception.SelfDeleteException;
import com.backend.social.repository.UserRepository;
import com.backend.social.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service

@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository)
    {
        this.userRepository=userRepository;
    }

    @Transactional
    public void deleteUserByIdentifier(String identifier) {

        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .or(() -> userRepository.findByMobile(identifier))
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with identifier: " + identifier)
                );

        Long currentAdminId = SecurityUtil.getCurrentUserId();

        if (user.getId().equals(currentAdminId)) {
            log.warn(
                    "Self-delete attempt blocked for admin. adminId={}",
                    currentAdminId
            );
            throw new SelfDeleteException("Admin cannot delete itself");
        }

        userRepository.delete(user);

        log.info(
                "Admin user deletion completed. adminId={}, deletedUserId={}",
                currentAdminId,
                user.getId()
        );
    }
}
