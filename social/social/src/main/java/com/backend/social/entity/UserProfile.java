package com.backend.social.entity;

import com.backend.social.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String fullName;

    @Column(length = 500)
    private String bio;

    private String profilePictureUrl;
    private String website;
    private String address;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dob;



}
