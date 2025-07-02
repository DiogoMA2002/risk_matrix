package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.dto.QuestionDTO;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.QuestionOption;
import ipleiria.risk_matrix.service.QuestionService;
import jakarta.validation.Valid;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO create(@RequestBody @Valid QuestionDTO dto) {
        return questionService.createQuestion(dto);
    }

    @GetMapping
    public List<Question> getAll() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/category/{categoryName}")
    public List<Question> getByCategory(@PathVariable String categoryName) {
        return questionService.getQuestionsByCategory(categoryName);
    }

    @GetMapping("/{id}")
    public QuestionDTO getById(@PathVariable Long id) {
        return questionService.getQuestionDtoById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO update(
            @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto) {
        return questionService.updateQuestion(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }
}
