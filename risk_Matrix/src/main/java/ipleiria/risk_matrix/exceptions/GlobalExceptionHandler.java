package ipleiria.risk_matrix.exceptions;

import ipleiria.risk_matrix.exceptions.exception.*;
import ipleiria.risk_matrix.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.IllegalArgumentException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(status.value(), status, message), status);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidOptionException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidOptionException(InvalidOptionException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(QuestionnaireNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleQuestionnaireNotFoundException(QuestionnaireNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCategoryException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidCategoryException(InvalidCategoryException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleQuestionNotFoundException(QuestionNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(FeedbackTooLongException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleFeedbackTooLongException(FeedbackTooLongException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder message = new StringBuilder("Validation failed: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; ");
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(400, HttpStatus.BAD_REQUEST, message.toString()));
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ex.printStackTrace(); // log to console
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(status.value(), status, "Internal Server Error: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

}
