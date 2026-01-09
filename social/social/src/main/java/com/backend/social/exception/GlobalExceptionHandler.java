package com.backend.social.exception;

import com.backend.social.dto.request.ApiError;
import com.backend.social.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* ===================== BUSINESS EXCEPTIONS ===================== */

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ErrorCode.USER_ALREADY_EXISTS,
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccountNotBlockedException.class)
    public ResponseEntity<ApiError> handleAccountNotBlocked(
            AccountNotBlockedException ex,
            HttpServletRequest request) {

        log.warn("Account not blocked action attempted. URI={}", request.getRequestURI());

        return buildError(
                HttpStatus.NOT_ACCEPTABLE,
                ErrorCode.ACCOUNT_NOT_BLOCKED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized access attempt. URI={}", request.getRequestURI());

        return buildError(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnverfiedAccountException.class)
    public ResponseEntity<ApiError> handleUnverifiedAccount(
            UnverfiedAccountException ex,
            HttpServletRequest request) {

        log.warn("Unverified account access blocked. URI={}", request.getRequestURI());

        return buildError(
                HttpStatus.FORBIDDEN,
                ErrorCode.UNVERIFIED_ACCOUNT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(SelfDeleteException.class)
    public ResponseEntity<ApiError> handleSelfDelete(
            SelfDeleteException ex,
            HttpServletRequest request) {

        log.warn("Self-delete attempt blocked. URI={}", request.getRequestURI());


        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.SELF_DELETE_NOT_ALLOWED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(CooldownException.class)
    public ResponseEntity<ApiError> handleOtpCooldown(
            CooldownException ex,
            HttpServletRequest request) {

        log.warn("OTP cooldown violation. URI={}", request.getRequestURI());

        return buildError(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.COOLDOWN_ACTIVE,
                ex.getMessage());
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ApiError> handleOtpRateLimit(
            LimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded. URI={}", request.getRequestURI());

        return buildError(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.LIMIT_EXCEEDED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage()
        );
    }





    @ExceptionHandler(InvalidContentException.class)
    public ResponseEntity<ApiError> handleNotFound(
            InvalidContentException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ErrorCode.INVALID_CONTENT,
                ex.getMessage()
        );
    }



    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNAUTHORIZED,   // or BAD_REQUEST
                ErrorCode.INVALID_CREDENTIALS,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ApiError> handleInactiveAccount(
            InactiveAccountException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNAUTHORIZED,   // or BAD_REQUEST
                ErrorCode.INACTIVE_ACCOUNT,
                ex.getMessage()
        );
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.FILE_SIZE_EXCEEDED,
                "File size must be less than 10MB"
        );
    }


    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(
            InvalidOtpException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_OTP,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(
            InvalidTokenException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_TOKEN,
                ex.getMessage()
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(
            TokenExpiredException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.TOKEN_EXPIRED,
                ex.getMessage()
        );
    }



    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(
            InvalidPasswordException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PASSWORD,
                ex.getMessage()
        );
    }

    /* ===================== VALIDATION ===================== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(),
                        err.getDefaultMessage()));

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Input validation failed"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidPayload(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Map<String, String> errorMap = new HashMap<>();

        String message = ex.getMessage();

        if (message != null) {

            if (message.contains("Gender")) {
                errorMap.put("gender", "Gender must be MALE, FEMALE, or OTHER");
            }

            if (message.contains("OtpType")) {
                errorMap.put("otpType", "Invalid OTP type. Allowed: EMAIL, MOBILE");
            }

            if (message.contains("OtpReason")) {
                errorMap.put("otpReason",
                        "Invalid OTP reason. Allowed: REGISTRATION, LOGIN, PASSWORD_RESET");
            }
        }

        // fallback message if no enum matched
        String finalMessage = errorMap.isEmpty()
                ? "Malformed JSON or invalid enum value"
                : "Invalid enum value";

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_BODY,
                finalMessage
        );
    }


    /* ===================== SYSTEM / FALLBACK ===================== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception occurred. URI={}",
                request.getRequestURI(),
                ex
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong"
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleDenied(AuthorizationDeniedException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "You are not allowed to access this resource"
        );
    }

    /* ===================== HELPER ===================== */

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            ErrorCode error,
            String message) {

        ApiError apiError = new ApiError(
                status.value(),
                error,
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(apiError);
    }


}
