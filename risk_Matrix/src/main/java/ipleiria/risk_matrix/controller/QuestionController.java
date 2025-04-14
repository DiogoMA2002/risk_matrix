package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // Criar uma nova pergunta
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO addQuestionToQuestionnaires(@RequestBody @Valid QuestionDTO questionDTO) {
        return questionService.createQuestion(questionDTO);
    }

    // Obter todas as perguntas
    @GetMapping("/all")
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }


    // Obter perguntas por categoria
    @GetMapping("/category/{categoryName}")
    public List<Question> getQuestionsByCategory(@PathVariable("categoryName") String categoryName) {
        return questionService.getQuestionsByCategory(categoryName);
    }
    // Obter pergunta por ID
    @GetMapping("/{id}")
    public Question getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    // Deletar uma pergunta por ID
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO updateQuestion(
            @PathVariable Long id,
            @RequestBody @Valid QuestionDTO updatedQuestionDTO) {
        return questionService.updateQuestion(id, updatedQuestionDTO);
    }



}
