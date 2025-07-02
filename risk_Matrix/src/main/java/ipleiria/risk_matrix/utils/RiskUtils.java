package ipleiria.risk_matrix.utils;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Severity;

import java.util.List;

public class RiskUtils {

    public static OptionLevel medianLevel(List<OptionLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }

        List<Integer> numeric = levels.stream()
                .map(OptionLevel::getValue)
                .sorted()
                .toList();

        int size = numeric.size();
        if (size % 2 == 1) {
            return OptionLevel.fromValue(numeric.get(size / 2));
        } else {
            int left = numeric.get(size / 2 - 1);
            int right = numeric.get(size / 2);
            int avg = Math.round((left + right) / 2.0f); // Fix: proper rounding
            return OptionLevel.fromValue(avg);
        }
    }

    // Compute severity using a matrix approach
    public static Severity computeSeverity(OptionLevel impact, OptionLevel probability) {
        if (impact == null || probability == null) {
            return Severity.UNKNOWN; // Changed from LOW to UNKNOWN for safety
        }

        int i = impact.getValue();
        int p = probability.getValue();

        // Matrix: [impact][probability]
        Severity[][] matrix = {
                // LOW     MEDIUM    HIGH     ← probability
                {Severity.LOW, Severity.LOW, Severity.MEDIUM},     // LOW impact
                {Severity.LOW, Severity.MEDIUM, Severity.HIGH},    // MEDIUM impact
                {Severity.MEDIUM, Severity.HIGH, Severity.CRITICAL} // HIGH impact
        };

        return matrix[i - 1][p - 1];
    }

    public static Severity computeCategorySeverity(List<AnswerDTO> answers) {
        List<AnswerDTO> filteredAnswers = answers.stream()
                .filter(a -> !"Não Aplicável".equalsIgnoreCase(a.getUserResponse()))
                .toList();

        if (filteredAnswers.isEmpty()) {
            return Severity.UNKNOWN;
        }

        List<OptionLevel> impactLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                .map(AnswerDTO::getChosenLevel)
                .toList();
        OptionLevel medianImpact = medianLevel(impactLevels);

        List<OptionLevel> probabilityLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                .map(AnswerDTO::getChosenLevel)
                .toList();
        OptionLevel medianProbability = medianLevel(probabilityLevels);

        return computeSeverity(medianImpact, medianProbability);
    }

    private RiskUtils() {}
}
