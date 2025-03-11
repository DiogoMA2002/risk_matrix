package ipleiria.risk_matrix.dto;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.models.answers.Probability;
import ipleiria.risk_matrix.models.answers.Serverity;

import java.util.List;
import java.util.stream.Collectors;

public class AnswerDTO {

    private Long id;
    private String userResponse;
    private Impact impact;
    private Probability probability;
    private Serverity serverity;
    private List<SuggestionDTO> suggestions;

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

    public Long getId() {
        return id;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public Impact getImpact() {
        return impact;
    }

    public Probability getProbability() {
        return probability;
    }

    public Serverity getServerity() {
        return serverity;
    }

    public List<SuggestionDTO> getSuggestions() {
        return suggestions;
    }
}
