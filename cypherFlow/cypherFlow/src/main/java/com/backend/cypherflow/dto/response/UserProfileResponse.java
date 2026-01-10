package com.backend.cypherflow.dto.response;


import com.backend.cypherflow.enums.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;

    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String website;
    private String location;

    private Gender gender;
    private LocalDate dob;


}

