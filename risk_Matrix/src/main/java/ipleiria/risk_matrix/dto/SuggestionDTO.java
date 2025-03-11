package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.sugestions.Suggestions;

public class SuggestionDTO {

    private String suggestionText;

    public SuggestionDTO() {}

    public SuggestionDTO(Suggestions suggestion) {
        this.suggestionText = suggestion.getSuggestionText();
    }

    public String getSuggestionText() {
        return suggestionText;
    }

    public void setSuggestionText(String suggestionText) {
        this.suggestionText = suggestionText;
    }
}
