package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "NOT_FOUND");
    }
}
