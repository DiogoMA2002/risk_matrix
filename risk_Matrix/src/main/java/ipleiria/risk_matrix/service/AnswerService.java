package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.models.sugestions.Suggestions;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // ✅ Create Answer based on input data
    public AnswerDTO submitAnswer(AnswerDTO answerDTO) {
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionOption selectedOption = question.getOptions().stream()
                .filter(option -> option.getOptionText().equals(answerDTO.getUserResponse()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid response"));

        Answer answer = new Answer();
        answer.setQuestionId(question.getId());
        answer.setQuestionText(question.getQuestionText());
        answer.setUserResponse(answerDTO.getUserResponse());
        answer.setImpact(selectedOption.getImpact());
        answer.setProbability(selectedOption.getProbability());
        answer.setSeverity(selectedOption.getSeverity());
        answer.setEmail(answerDTO.getEmail());

        answerRepository.save(answer);

        // ✅ Return suggestions from the selected option
        return new AnswerDTO(answer, selectedOption.getSuggestions());
    }

    public List<AnswerDTO> getAnswersByEmail(String email) {
        return answerRepository.findByEmail(email).stream()
                .map(answer -> {
                    // ✅ Get suggestions from QuestionOption
                    List<Suggestions> suggestions = questionRepository.findById(answer.getQuestionId())
                            .flatMap(question -> question.getOptions().stream()
                                    .filter(option -> option.getOptionText().equals(answer.getUserResponse()))
                                    .findFirst())
                            .map(QuestionOption::getSuggestions)
                            .orElse(new ArrayList<>());

                    return new AnswerDTO(answer, suggestions);
                })
                .toList();
    }

    public List<AnswerDTO> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(answer -> {
                    // ✅ Get suggestions from QuestionOption
                    List<Suggestions> suggestions = questionRepository.findById(answer.getQuestionId())
                            .flatMap(question -> question.getOptions().stream()
                                    .filter(option -> option.getOptionText().equals(answer.getUserResponse()))
                                    .findFirst())
                            .map(QuestionOption::getSuggestions)
                            .orElse(new ArrayList<>());

                    return new AnswerDTO(answer, suggestions);
                })
                .collect(Collectors.toList());
    }

    // Buscar todas as respostas
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

}
