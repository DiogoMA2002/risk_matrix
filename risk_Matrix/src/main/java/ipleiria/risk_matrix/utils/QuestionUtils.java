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

}