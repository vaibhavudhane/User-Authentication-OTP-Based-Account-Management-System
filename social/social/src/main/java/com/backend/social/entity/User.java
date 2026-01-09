package com.backend.social.entity;

import com.backend.social.enums.AccountStatus;
import com.backend.social.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String mobile;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private boolean emailVerified;
    private boolean mobileVerified;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private LocalDateTime lockUntil;
    private int failedLoginAttempts;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean passwordResetAllowed = false;

    @Column
    private LocalDateTime passwordResetExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private  Role role;

}







