package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_CONFLICT");
    }
}
