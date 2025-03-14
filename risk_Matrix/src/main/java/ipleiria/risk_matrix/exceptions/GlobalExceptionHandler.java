package ipleiria.risk_matrix.exceptions;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import ipleiria.risk_matrix.exceptions.exception.*;
import ipleiria.risk_matrix.responses.ErrorResponse;



@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(status.value(), status, ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }
    @ExceptionHandler(InvalidOptionException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidUserParams(InvalidOptionException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(status.value(), status, ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }



}