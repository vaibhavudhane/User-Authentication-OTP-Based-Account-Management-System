package com.backend.cypherflow.service;

import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.exception.ResourceNotFoundException;
import com.backend.cypherflow.exception.SelfDeleteException;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    // ---------- HELPERS ----------

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user");
        u.setEmail("user@gmail.com");
        u.setMobile("9999999999");
        return u;
    }

    // ---------- TC-01: USER NOT FOUND ----------

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByMobile(any()))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThrows(ResourceNotFoundException.class,
                    () -> adminService.deleteUserByIdentifier("unknown"));
        }
    }

    // ---------- TC-02: SELF DELETE ----------

    @Test
    void deleteUser_shouldThrowException_whenAdminDeletesSelf() {

        User admin = user(1L);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(admin));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThrows(SelfDeleteException.class,
                    () -> adminService.deleteUserByIdentifier("admin"));

            verify(userRepository, never()).delete(any());
        }
    }

    // ---------- TC-03: DELETE BY USERNAME ----------

    @Test
    void deleteUser_shouldDeleteUser_whenFoundByUsername() {

        User user = user(2L);

        when(userRepository.findByUsernameIgnoreCase("user"))
                .thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            adminService.deleteUserByIdentifier("user");

            verify(userRepository).delete(user);
        }
    }

    // ---------- TC-04: DELETE BY EMAIL ----------

    @Test
    void deleteUser_shouldDeleteUser_whenFoundByEmail() {

        User user = user(2L);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@gmail.com"))
                .thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            adminService.deleteUserByIdentifier("user@gmail.com");

            verify(userRepository).delete(user);
        }
    }

    // ---------- TC-05: DELETE BY MOBILE ----------

    @Test
    void deleteUser_shouldDeleteUser_whenFoundByMobile() {

        User user = user(2L);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByMobile("9999999999"))
                .thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            adminService.deleteUserByIdentifier("9999999999");

            verify(userRepository).delete(user);
        }
    }
}

