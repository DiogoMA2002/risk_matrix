package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class FeedbackTooLongException extends BaseException {
    public FeedbackTooLongException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "FEEDBACK_TOO_LONG");
    }
}
