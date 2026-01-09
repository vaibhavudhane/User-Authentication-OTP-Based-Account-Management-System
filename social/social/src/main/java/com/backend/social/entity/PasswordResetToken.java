package com.backend.social.entity;

import com.backend.social.enums.SecurityActionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int requestCount;

    private LocalDateTime firstRequestAt;

    private LocalDateTime lastRequestAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private SecurityActionType actionType;

    @Column(name = "action_used")
    private boolean actionUsed;

}

