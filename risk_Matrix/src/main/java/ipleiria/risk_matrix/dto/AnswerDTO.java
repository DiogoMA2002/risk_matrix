package ipleiria.risk_matrix.dto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Serverity;
import ipleiria.risk_matrix.models.sugestions.Suggestions;

import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({
        "id",
        "userResponse",
        "impact",
        "probability",
        "serverity",
        "suggestions"
})
public class AnswerDTO {

    private Long id;
    private String userResponse;
    private Impact impact;
    private Probability probability;
    private Serverity serverity;
    private List<SuggestionDTO> suggestions;

    // ✅ Default constructor (needed for Jackson)
    public AnswerDTO() {}

    // ✅ Constructor for output
    public AnswerDTO(Answer answer) {
        this.id = answer.getId();
        this.userResponse = answer.getUserResponse();
        this.impact = answer.getImpact();
        this.probability = answer.getProbability();
        this.serverity = answer.getServerity();

        if (answer.getSuggestions() != null) {
            this.suggestions = answer.getSuggestions().stream()
                    .map(SuggestionDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // ✅ Constructor for input (Deserialization)
    @JsonCreator
    public AnswerDTO(
            @JsonProperty("userResponse") String userResponse,
            @JsonProperty("impact") Impact impact,
            @JsonProperty("probability") Probability probability) {
        this.userResponse = userResponse;
        this.impact = impact;
        this.probability = probability;
    }

    // ✅ Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserResponse() { return userResponse; }
    public void setUserResponse(String userResponse) { this.userResponse = userResponse; }

    public Impact getImpact() { return impact; }
    public void setImpact(Impact impact) { this.impact = impact; }

    public Probability getProbability() { return probability; }
    public void setProbability(Probability probability) { this.probability = probability; }

    public Serverity getServerity() { return serverity; }
    public void setServerity(Serverity serverity) { this.serverity = serverity; }

    public List<SuggestionDTO> getSuggestions() { return suggestions; }
    public void setSuggestions(List<SuggestionDTO> suggestions) { this.suggestions = suggestions; }
}
