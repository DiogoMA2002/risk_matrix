package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;

public final class ReportRecommendationResolver {

    private ReportRecommendationResolver() {}

    public static String resolve(Answer answer, Question question) {
        if (question == null || question.getOptions() == null || question.getOptions().isEmpty()) {
            return "-";
        }

        QuestionOption matched = findMatchingOption(answer, question);
        if (matched == null) {
            return "-";
        }

        String recommendation = matched.getRecommendation();
        return recommendation != null && !recommendation.isBlank() ? recommendation : "-";
    }

    private static QuestionOption findMatchingOption(Answer answer, Question question) {
        if (answer.getQuestionOptionId() != null) {
            for (QuestionOption option : question.getOptions()) {
                if (answer.getQuestionOptionId().equals(option.getId())) {
                    return option;
                }
            }
        }

        if (answer.getUserResponse() != null && !answer.getUserResponse().isBlank()) {
            String response = answer.getUserResponse().trim();
            for (QuestionOption option : question.getOptions()) {
                if (option.getOptionText() != null
                        && option.getOptionText().equalsIgnoreCase(response)) {
                    return option;
                }
            }
        }

        return null;
    }
}
