package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.FeedbackDTO;
import ipleiria.risk_matrix.dto.FeedbackRequestDTO;
import ipleiria.risk_matrix.exceptions.exception.InvalidFeedbackTypeException;
import ipleiria.risk_matrix.models.feedback.Feedback;
import ipleiria.risk_matrix.models.feedback.FeedbackType;
import ipleiria.risk_matrix.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback", description = "Submit and manage user feedback (suggestions, help requests)")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @Operation(summary = "Submit feedback", description = "Submits user feedback (suggestion or help request). Accessible by ADMIN and PUBLIC roles.")
    @ApiResponse(responseCode = "200", description = "Feedback saved")
    @ApiResponse(responseCode = "400", description = "Invalid feedback data")
    public ResponseEntity<FeedbackDTO> createFeedback(@Valid @RequestBody FeedbackRequestDTO dto) {
        return ResponseEntity.ok(new FeedbackDTO(feedbackService.saveFeedback(dto)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all feedback (paginated)", description = "Returns all feedback entries sorted by creation date descending. Requires ADMIN role.")
    public ResponseEntity<Page<FeedbackDTO>> getAllFeedback(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 200)") @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(feedbackService.getAllFeedback(page, size).map(FeedbackDTO::new));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Filter feedback", description = "Filters feedback by email, type, and/or date range. Requires ADMIN role.")
    public ResponseEntity<List<FeedbackDTO>> filterFeedback(
            @Parameter(description = "Email filter") @RequestParam(required = false) String email,
            @Parameter(description = "Feedback type (SUGGESTION, HELP)") @RequestParam(required = false) String type,
            @Parameter(description = "Start date (ISO date-time)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO date-time)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        FeedbackType feedbackType = null;
        if (type != null && !type.isBlank()) {
            feedbackType = parseFeedbackType(type);
        }

        return ResponseEntity.ok(feedbackService.filterFeedback(email, feedbackType, startDate, endDate)
                .stream()
                .map(FeedbackDTO::new)
                .toList());
    }

    private FeedbackType parseFeedbackType(String type) {
        try {
            return FeedbackType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFeedbackTypeException("Invalid feedback type: " + type);
        }
    }
}
