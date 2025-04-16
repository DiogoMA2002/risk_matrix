package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class QuestionNotFoundException extends BaseException {
    public QuestionNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "QUESTION_NOT_FOUND");
    }
}
