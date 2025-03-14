package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // <— 400 instead of 401
public class InvalidOptionException extends RuntimeException {
    public InvalidOptionException(String message) {
        super(message);
    }
}
