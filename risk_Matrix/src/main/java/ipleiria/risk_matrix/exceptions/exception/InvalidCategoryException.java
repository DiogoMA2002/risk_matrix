package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class InvalidCategoryException extends BaseException {
    public InvalidCategoryException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
    }}
