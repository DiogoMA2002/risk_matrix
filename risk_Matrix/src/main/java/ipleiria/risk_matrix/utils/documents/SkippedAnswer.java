package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;

public record SkippedAnswer(Long questionId, String questionText, String reason) {

    public static SkippedAnswer missingQuestion(Answer answer) {
        return new SkippedAnswer(
                answer.getQuestionId(),
                safeQuestionText(answer),
                "pergunta não encontrada na base de dados");
    }

    public static SkippedAnswer missingCategory(Answer answer) {
        return new SkippedAnswer(
                answer.getQuestionId(),
                safeQuestionText(answer),
                "categoria em falta para a pergunta");
    }

    private static String safeQuestionText(Answer answer) {
        return answer.getQuestionText() != null && !answer.getQuestionText().isBlank()
                ? answer.getQuestionText()
                : "sem texto";
    }
}
