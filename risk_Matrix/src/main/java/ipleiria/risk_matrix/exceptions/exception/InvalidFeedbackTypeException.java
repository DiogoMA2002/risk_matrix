package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class InvalidFeedbackTypeException extends BaseException {
    public InvalidFeedbackTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_FEEDBACK_TYPE");
    }
}
