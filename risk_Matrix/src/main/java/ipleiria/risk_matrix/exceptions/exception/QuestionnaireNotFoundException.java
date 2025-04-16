package ipleiria.risk_matrix.exceptions.exception;

import org.springframework.http.HttpStatus;

public class QuestionnaireNotFoundException extends BaseException {
    public QuestionnaireNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "QUESTIONNAIRE_NOT_FOUND");
    }
}
