package ipleiria.risk_matrix.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final int statusCode;
    private final HttpStatus status;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String path;
    private final Map<String, List<String>> validationErrors;

    public ErrorResponse(int statusCode, HttpStatus status, String message, String errorCode, String path, Map<String, List<String>> validationErrors) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.validationErrors = validationErrors;
    }
}