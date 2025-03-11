package ipleiria.risk_matrix.dto;
import ipleiria.risk_matrix.models.answers.Answer;

import java.util.List;
import java.util.stream.Collectors;

public class AnswerDTO {

    private Long id;
    private String userResponse;
    private List<SuggestionDTO> suggestions;

    public AnswerDTO() {}

    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.userResponse = answer.getUserResponse();
        this.suggestions = answer.getSuggestions().stream()
                .map(SuggestionDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public List<SuggestionDTO> getSuggestions() {
        return suggestions;
    }
}
