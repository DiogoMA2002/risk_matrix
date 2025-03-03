package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.RiskLevel;
import ipleiria.risk_matrix.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // Submeter uma resposta
    @PostMapping("/submit/{questionId}")
    public Answer submitAnswer(@PathVariable Long questionId, @RequestParam String userResponse) {
        return answerService.submitAnswer(questionId, userResponse);
    }

    // Obter todas as respostas
    @GetMapping("/all")
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    // Obter respostas filtradas por nível de risco
    @GetMapping("/risk/{riskLevel}")
    public List<Answer> getAnswersByRiskLevel(@PathVariable RiskLevel riskLevel) {
        return answerService.getAnswersByRiskLevel(riskLevel);
    }
}
