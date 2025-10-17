package com.example.commonservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    int status;
    String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String fieldName;

    String path;
    private List<ApiSubError> errors;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(int status, String message, String path, List<ApiSubError> errors) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public ApiError(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = Collections.emptyList();
    }

    public ApiError(String errorCode, String message, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.errors = Collections.emptyList();
    }

    public ApiError(String errorCode, int status, String message, String path) {
        this.errorCode = errorCode;;
        this.status = status;
        this.message = message;
        this.path = path;
    }
}
