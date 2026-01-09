package com.backend.social.dto.request;

import com.backend.social.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 3, max = 100, message = "Full name must be 3 to 100 characters")
    private String fullName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String website;

    private String address;
    private Gender gender;
    private LocalDate dob;
}

