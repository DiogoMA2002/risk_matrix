package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String resource, Long id) {
        super(String.format("%s with id %d not found", resource, id), HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String resource, String identifier) {
        super(String.format("%s with identifier %s not found", resource, identifier), HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
