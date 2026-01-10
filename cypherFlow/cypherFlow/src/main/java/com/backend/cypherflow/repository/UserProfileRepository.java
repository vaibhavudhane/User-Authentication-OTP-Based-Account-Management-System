package com.backend.cypherflow.repository;

import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userid);
    void deleteByUserId(Long userId);
}
