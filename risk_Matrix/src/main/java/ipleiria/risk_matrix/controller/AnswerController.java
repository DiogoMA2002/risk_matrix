package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.answers.RiskLevel;
import ipleiria.risk_matrix.service.AnswerService;
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

    // Submeter uma resposta
    @PostMapping("/submit/{questionId}")
    public ResponseEntity<Answer> submitAnswer(@PathVariable Long questionId, @RequestBody Map<String, String> request) {
        String userResponse = request.get("userResponse");
        if (userResponse == null) {
            return ResponseEntity.badRequest().build(); // Retorna erro se a resposta for nula
        }
        Answer savedAnswer = answerService.submitAnswer(questionId, userResponse);
        return ResponseEntity.ok(savedAnswer);
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
