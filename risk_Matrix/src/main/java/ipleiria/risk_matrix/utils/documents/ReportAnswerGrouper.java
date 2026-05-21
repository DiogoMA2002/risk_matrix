package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReportAnswerGrouper {

    private ReportAnswerGrouper() {}

    public record GroupingResult(
            Map<String, List<Answer>> answersByCategory,
            List<SkippedAnswer> skippedAnswers
    ) {}

    public static GroupingResult groupByCategory(List<Answer> answers, Map<Long, Question> questionMap) {
        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        List<SkippedAnswer> skippedAnswers = new ArrayList<>();

        for (Answer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                skippedAnswers.add(SkippedAnswer.missingQuestion(answer));
                continue;
            }
            if (question.getCategory() == null || question.getCategory().getName() == null) {
                skippedAnswers.add(SkippedAnswer.missingCategory(answer));
                continue;
            }

            String category = question.getCategory().getName();
            answersByCategory.computeIfAbsent(category, _ -> new ArrayList<>()).add(answer);
        }

        return new GroupingResult(answersByCategory, skippedAnswers);
    }
}
