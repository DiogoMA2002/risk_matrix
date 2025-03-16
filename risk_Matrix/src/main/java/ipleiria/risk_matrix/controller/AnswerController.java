package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.dto.UserAnswersDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // Obter todas as respostas
    @GetMapping("/all")
    public List<AnswerDTO> getAllAnswers() {
        return answerService.getAllAnswers(); // ✅ Agora os tipos estão alinhados
    }

    @PostMapping("/submit")
    public AnswerDTO submitAnswer(@RequestBody AnswerDTO answerDTO) {
        return answerService.submitAnswer(answerDTO);
    }

    @GetMapping("/by-question/{questionId}")
    public List<AnswerDTO> getAnswersByQuestion(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestion(questionId);
    }

    // ✅ Get all answers by user email
    @GetMapping("/by-email/{email}")
    public List<AnswerDTO> getAnswersByEmail(@PathVariable String email) {
        return answerService.getAnswersByEmail(email);
    }
    @GetMapping("/by-email-with-severity/{email}")
    public UserAnswersDTO getUserAnswersWithSeverity(@PathVariable String email) {
        return answerService.getUserAnswersWithSeverities(email);
    }
    @GetMapping("/get-all-email")
    public List<UserAnswersDTO> getAllAnswersWithSeverityAndEmail() {
        return answerService.getAllAnswersWithSeverityAndEmail();
    }
    @PostMapping("/submit-multiple")
    public List<AnswerDTO> submitMultipleAnswers(@RequestBody List<AnswerDTO> answers) {
        return answerService.submitMultipleAnswers(answers);
    }

}
