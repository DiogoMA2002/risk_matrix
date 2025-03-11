package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.Impact;
import ipleiria.risk_matrix.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // Obter todas as respostas
    @GetMapping("/all")
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }


    @PostMapping("/submit")
    public AnswerDTO submitAnswer(@RequestBody AnswerDTO answerDTO) {
        return answerService.submitAnswer(answerDTO);
    }

    @GetMapping("/by-question/{questionId}")
    public List<AnswerDTO> getAnswersByQuestion(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestion(questionId);
    }

    @GetMapping("/by-email/{email}")
    public List<AnswerDTO> getAnswersByEmail(@PathVariable String email) {
        return answerService.getAnswersByEmail(email);
    }
}
