package com.example.commonservice.exception_handler.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DuplicateException extends RuntimeException {
    private String fieldName;
    public DuplicateException(String message) {
        super(message);
    }
    public DuplicateException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
