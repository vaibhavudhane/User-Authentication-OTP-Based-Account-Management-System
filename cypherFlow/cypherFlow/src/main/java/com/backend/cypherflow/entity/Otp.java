package com.backend.cypherflow.entity;

import com.backend.cypherflow.enums.OtpReason;
import com.backend.cypherflow.enums.OtpStatus;
import com.backend.cypherflow.enums.OtpType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String otpHash;

    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    @Enumerated(EnumType.STRING)
    private OtpReason otpReason;

    @Enumerated(EnumType.STRING)
    private OtpStatus status;

    private LocalDateTime expiresAt;
    private int retryCount;
    private LocalDateTime lastSentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;


}






