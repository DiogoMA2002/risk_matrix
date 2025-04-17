package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class handleDuplicateException extends BaseException {
    public handleDuplicateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "DUPLICATE_QUESTION");
    }
}
