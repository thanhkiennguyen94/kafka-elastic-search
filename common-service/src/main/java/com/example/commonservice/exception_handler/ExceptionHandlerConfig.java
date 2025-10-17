package com.example.commonservice.exception_handler;

import com.example.commonservice.entity.ApiError;
import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.ApiSubError;
import com.example.commonservice.exception_handler.exception.*;
import com.example.commonservice.util.ConstantUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.*;

@Slf4j
@RestControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ExceptionHandlerConfig {

    @ExceptionHandler(SystemErrorException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleSystemError(Exception ex, HttpServletRequest request) {
        log.error("Lỗi hệ thống >>>>>>>>>>>>>>>>>>>: ", ex);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ApiError error = new ApiError(
                status,
                ConstantUtils.SYSTEM_ERROR_MSG,
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status, error, ConstantUtils.SYSTEM_ERROR_MSG), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleAll(Exception ex, HttpServletRequest request) {
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw (RuntimeException) ex;
        }
        log.error("Lỗi hệ thống >>>>>>>>>>>>>>>>>>>: ", ex);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ApiError error = new ApiError(
                status,
                ConstantUtils.SYSTEM_ERROR_MSG,
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status, error, ConstantUtils.SYSTEM_ERROR_MSG), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidArgument(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, FieldError> fieldErrorMap = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String[] codes = error.getCodes(); // array of codes

            if (!fieldErrorMap.containsKey(field)) {
                fieldErrorMap.put(field, error);
            } else {
                FieldError existing = fieldErrorMap.get(field);
                if (isHigherPriority(codes, existing.getCodes())) {
                    fieldErrorMap.put(field, error);
                }
            }
        }

        List<ApiSubError> fieldErrors = fieldErrorMap.values()
                .stream()
                .map(error -> new ApiSubError(error.getField(), error.getDefaultMessage()))
                .toList();

        List<ApiSubError> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(error -> new ApiSubError(error.getObjectName(), error.getDefaultMessage()))
                .toList();

        List<ApiSubError> errors = new ArrayList<>();
        errors.addAll(fieldErrors);
        errors.addAll(globalErrors);
        int status = HttpStatus.BAD_REQUEST.value();
        ApiError response = new ApiError(
                status,
                ConstantUtils.VALIDATE_ERROR_MSG,
                request.getRequestURI(),
                errors
        );

        return new ResponseEntity<>(ApiResponse.error(status, response, ConstantUtils.VALIDATE_ERROR_MSG), HttpStatus.BAD_REQUEST);
    }

    private boolean isHigherPriority(String[] newCodes, String[] existingCodes) {
        List<String> priority = List.of("NotBlank", "NotNull", "NotEmpty", "Size", "Pattern");

        int newPriority = getFirstMatchingPriorityIndex(newCodes, priority);
        int existingPriority = getFirstMatchingPriorityIndex(existingCodes, priority);

        return newPriority >= 0 && (existingPriority == -1 || newPriority < existingPriority);
    }

    private int getFirstMatchingPriorityIndex(String[] codes, List<String> priority) {
        for (String code : codes) {
            for (int i = 0; i < priority.size(); i++) {
                if (code.contains(priority.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    //    xử lý exception @RequestParam, @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidArgument(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiSubError> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    if (field == null || field.isBlank()) {
                        field = violation.getRootBeanClass().getSimpleName();
                    }
                    return new ApiSubError(field, violation.getMessage());
                })
                .toList();
        int status = HttpStatus.BAD_REQUEST.value();
        ApiError res = new ApiError(
                status,
                ConstantUtils.VALIDATE_ERROR_MSG,
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(ApiResponse.error(status, res, ConstantUtils.VALIDATE_ERROR_MSG), HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(InvalidOldPasswordException.class)
//    public ResponseEntity<ApiResponse<ApiError>> handleInvalidOldPasswordException(
//            InvalidOldPasswordException ex,
//            HttpServletRequest request
//    ) {
//        ApiError apiError = new ApiError(
//                HttpStatus.BAD_REQUEST.value(),
//                ex.getMessage(),
//                request.getRequestURI(),
//                null
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
//    }

    //    handle property name
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidArgument(PropertyReferenceException ex, HttpServletRequest request) {
        int status = HttpStatus.BAD_REQUEST.value();
        ApiError error = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status, error, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    //    exception custom handle
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidArgument(NotFoundException ex, HttpServletRequest request) {
        int status = HttpStatus.NOT_FOUND.value();
        ApiError error = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status, error, ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    //    exception duplicate data
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidArgument(DuplicateException ex, HttpServletRequest request) {
        int status = HttpStatus.CONFLICT.value();
        ApiError error = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );

        if (ex.getFieldName() != null) {
            error.setFieldName(ex.getFieldName());
        }

        return new ResponseEntity<>(ApiResponse.error(status,error,ex.getMessage()), HttpStatus.CONFLICT);
    }

    //    check body null response 400 thay vì 401
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBadRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        int status = HttpStatus.BAD_REQUEST.value();
        ApiError error = new ApiError(
                status,
                ConstantUtils.BODY_ERROR_MSG,
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status,error,ConstantUtils.BODY_ERROR_MSG), HttpStatus.BAD_REQUEST);
    }

    //   xử lý url không tồn tại thì báo 404 thay vì 401
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        int status = HttpStatus.NOT_FOUND.value();
        ApiError error = new ApiError(
                status,
                ConstantUtils.URL_NOT_FOUND_ERROR_MSG,
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status,error, ConstantUtils.URL_NOT_FOUND_ERROR_MSG), HttpStatus.NOT_FOUND);
    }

    //    handle exception sai ten dang nhap hoac mat khau
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        int status = HttpStatus.UNAUTHORIZED.value();

        ApiError apiError = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status, apiError, ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    //    fix loi feign long exception
    @ExceptionHandler(HttpResponseException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleHttpResponse(HttpResponseException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                ex.getStatus(),
                ex.getMessage(),
                ex.getPath()
        );
        return new ResponseEntity<>(ApiResponse.error(ex.getStatus(), error, ex.getMessage()), HttpStatus.valueOf(ex.getStatus()));
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleAuthenticationServiceException(AuthenticationServiceException ex, HttpServletRequest request) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

        ApiError apiError = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status,apiError, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidDataException(InvalidDataException ex, HttpServletRequest request) {
        int status = HttpStatus.BAD_REQUEST.value();
        ApiError error = new ApiError(
                status,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(ApiResponse.error(status,error,ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
