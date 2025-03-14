package ipleiria.risk_matrix.responses;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private int errorCode;
    private HttpStatus status;
    private String message;

    // Constructor
    public ErrorResponse(int errorCode, HttpStatus status, String message) {
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
    }

    public ErrorResponse() {

    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}