package com.backend.cypherflow.dto.request;

import com.backend.cypherflow.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ApiError {

    private final int status;
    private final ErrorCode error;
    private final String message;
    private final LocalDateTime timestamp;

}
