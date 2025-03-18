package ipleiria.risk_matrix.utils;

import ipleiria.risk_matrix.models.questions.*;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionUtils {

    // Utility method to ensure "Não Aplicável" is present
    public static void ensureNaoAplicavelOption(Question question) {
        boolean hasNaoAplicavel = question.getOptions().stream()
                .anyMatch(opt -> "Não Aplicável".equalsIgnoreCase(opt.getOptionText()));

        if (!hasNaoAplicavel) {
            QuestionOption naoAplicavel = new QuestionOption();
            naoAplicavel.setOptionText("Não Aplicável");
            naoAplicavel.setOptionType(OptionLevelType.IMPACT); // or your default
            naoAplicavel.setOptionLevel(OptionLevel.LOW);       // or your default
            naoAplicavel.setQuestion(question);
            question.getOptions().add(naoAplicavel);
        }
    }

    // Helper: compute median of a list of levels
    public OptionLevel medianLevel(List<OptionLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null; // or handle "no data" scenario
        }

        // Convert LOW=1, MEDIUM=2, HIGH=3
        List<Integer> numeric = levels.stream()
                .map(this::levelToInt)
                .sorted()
                .collect(Collectors.toList());

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

    // Helper: convert OptionLevel -> int
    public int levelToInt(OptionLevel level) {
        return switch (level) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    // Helper: convert int -> OptionLevel
    public OptionLevel intToLevel(int val) {
        return switch (val) {
            case 1 -> OptionLevel.LOW;
            case 2 -> OptionLevel.MEDIUM;
            case 3 -> OptionLevel.HIGH;
            default -> null;
        };
    }

    // Helper: cross IMPACT x PROBABILITY -> Severity
    public Severity computeSeverity(OptionLevel impact, OptionLevel probability) {
        // If either is null, default to something
        if (impact == null || probability == null) {
            return Severity.LOW; // or handle differently
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
}