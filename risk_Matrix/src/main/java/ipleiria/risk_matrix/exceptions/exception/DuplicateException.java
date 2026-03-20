package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateException extends BaseException {
    public DuplicateException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_ENTRY");
    }
}
