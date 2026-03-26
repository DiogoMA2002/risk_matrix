package ipleiria.risk_matrix.exceptions;

import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.exceptions.exception.RateLimitExceededException;
import ipleiria.risk_matrix.responses.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    void handleBaseException_returnsConflictResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(new ConflictException("already exists"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void handleBadCredentialsException_returnsUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(
                new BadCredentialsException("bad creds"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_CREDENTIALS");
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("bad request"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("bad request");
    }

    @Test
    void handleDataIntegrityViolation_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("constraint"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("DATA_INTEGRITY_VIOLATION");
    }

    @Test
    void handleConstraintViolation_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(
                new ConstraintViolationException("violated", null), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONSTRAINT_VIOLATION");
    }

    @Test
    void handleMissingRequestParameter_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameter(
                new MissingServletRequestParameterException("page", "int"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("MISSING_REQUEST_PARAMETER");
    }

    @Test
    void handleMaxUploadSizeExceeded_returnsPayloadTooLarge() {
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceeded(
                new MaxUploadSizeExceededException(1024), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("MAX_UPLOAD_SIZE_EXCEEDED");
    }

    @Test
    void handleRateLimitExceeded_returnsTooManyRequests() {
        ResponseEntity<ErrorResponse> response = handler.handleRateLimitExceeded(
                new RateLimitExceededException("Too many requests"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }
}
