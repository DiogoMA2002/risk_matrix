package ipleiria.risk_matrix.utils;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.OptionLevelType;
import ipleiria.risk_matrix.models.questions.Severity;

import java.util.List;
import java.util.stream.Collectors;

public class RiskUtils {

    // Utility method to compute the median of a list of OptionLevels
    public static OptionLevel medianLevel(List<OptionLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null; // or handle "no data" scenario
        }

        // Convert LOW=1, MEDIUM=2, HIGH=3
        List<Integer> numeric = levels.stream()
                .map(RiskUtils::levelToInt)
                .sorted()
                .toList();

        int size = numeric.size();
        if (size % 2 == 1) {
            // Odd
            return intToLevel(numeric.get(size / 2));
        } else {
            // Even
            int left = numeric.get(size / 2 - 1);
            int right = numeric.get(size / 2);
            int avg = (left + right) / 2;
            return intToLevel(avg);
        }
    }

    // Convert OptionLevel -> int
    public static int levelToInt(OptionLevel level) {
        return switch (level) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    // Convert int -> OptionLevel
    public static OptionLevel intToLevel(int val) {
        return switch (val) {
            case 1 -> OptionLevel.LOW;
            case 2 -> OptionLevel.MEDIUM;
            case 3 -> OptionLevel.HIGH;
            default -> null;
        };
    }

    // Cross IMPACT x PROBABILITY -> Severity
    public static Severity computeSeverity(OptionLevel impact, OptionLevel probability) {
        // If either is null, default to LOW
        if (impact == null || probability == null) {
            return Severity.LOW;
        }

        return switch (impact) {
            case HIGH -> switch (probability) {
                case HIGH -> Severity.CRITICAL;
                case MEDIUM -> Severity.HIGH;
                case LOW -> Severity.MEDIUM;
            };
            case MEDIUM -> switch (probability) {
                case HIGH -> Severity.HIGH;
                case MEDIUM -> Severity.MEDIUM;
                case LOW -> Severity.LOW;
            };
            case LOW -> switch (probability) {
                case HIGH -> Severity.MEDIUM;
                case MEDIUM, LOW -> Severity.LOW;
            };
        };
    }
    public static Severity computeCategorySeverity(List<AnswerDTO> answers) {
        List<AnswerDTO> filteredAnswers = answers.stream()
                .filter(a -> !"Não Aplicável".equalsIgnoreCase(a.getUserResponse()))
                .toList();
        if (filteredAnswers.isEmpty()) {
            return Severity.LOW;
        }

        // Compute median for IMPACT levels
        List<OptionLevel> impactLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.IMPACT)
                .map(AnswerDTO::getChosenLevel)
                .collect(Collectors.toList());
        OptionLevel medianImpact = medianLevel(impactLevels);

        // Compute median for PROBABILITY levels
        List<OptionLevel> probabilityLevels = filteredAnswers.stream()
                .filter(a -> a.getQuestionType() == OptionLevelType.PROBABILITY)
                .map(AnswerDTO::getChosenLevel)
                .collect(Collectors.toList());
        OptionLevel medianProbability = medianLevel(probabilityLevels);

        return computeSeverity(medianImpact, medianProbability);
    }
    // Private constructor to prevent instantiation
    private RiskUtils() {}
}