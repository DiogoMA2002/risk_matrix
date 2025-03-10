package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.sugestions.Suggestions;

public class SuggestionDTO {

    private Long id;
    private String suggestionText;

    public SuggestionDTO() {}

    public SuggestionDTO(Suggestions suggestion) {
        this.id = suggestion.getId();
        this.suggestionText = suggestion.getSuggestionText();
    }

    public Long getId() {
        return id;
    }

    public String getSuggestionText() {
        return suggestionText;
    }
}
