package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionCategory;
import ipleiria.risk_matrix.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Criar uma nova pergunta
    @PostMapping("/add/{questionnaireId}")
    public QuestionDTO addQuestionToQuestionnaire(
            @PathVariable Long questionnaireId,
            @RequestBody QuestionDTO questionDTO) {
        return questionService.createQuestion(questionnaireId, questionDTO);
    }

    // Obter todas as perguntas
    @GetMapping("/all")
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/all/suggestions")
    public List<QuestionDTO> getAllQuestionsWithSuggestion() {
        return questionService.getAllQuestionsWithSuggestion();
    }


    // Obter perguntas por categoria
    @GetMapping("/category/{category}")
    public List<Question> getQuestionsByCategory(@PathVariable QuestionCategory category) {
        return questionService.getQuestionsByCategory(category);
    }

    // Obter pergunta por ID
    @GetMapping("/{id}")
    public Optional<Question> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    // Deletar uma pergunta por ID
    @DeleteMapping("/delete/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @PutMapping("/{id}")
    public QuestionDTO updateQuestion(@PathVariable Long id, @RequestBody QuestionDTO updatedQuestionDTO) {
        return questionService.updateQuestion(id, updatedQuestionDTO);
    }


}
