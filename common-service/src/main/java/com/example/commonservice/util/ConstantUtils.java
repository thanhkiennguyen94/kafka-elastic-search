package com.example.commonservice.util;


public class ConstantUtils {

    private ConstantUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static final String BEARER = "Bearer ";
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_CURRENT_PAGE = 0;
    public static final int MIN_CURRENT_PAGE = 0;
    public static final String PAGE_MIN_MSG = "Page must not be less than " + MIN_CURRENT_PAGE;
    public static final String SIZE_MIN_MSG = "Size must not be less than " + MIN_PAGE_SIZE;
    public static final String SIZE_MAX_MSG = "Size must not be greater than " + MAX_PAGE_SIZE;

    public static final String SYSTEM_ERROR_MSG = "System Error";
    public static final String VALIDATE_ERROR_MSG = "Validate Error";
    public static final String BODY_ERROR_MSG = "Invalid or empty request body";
    public static final String URL_NOT_FOUND_ERROR_MSG = "Not Found";

    public static final String CUSTOM_EXCEPTION = "Custom exception: {}";
    public static final String CREATE_SUCCESSFULLY = "Create successfully";
    public static final String UPDATE_SUCCESSFULLY = "Update successfully";
    public static final String DELETE_SUCCESSFULLY = "Delete successfully";

}
