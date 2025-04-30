package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.RiskUtils.computeCategorySeverity;

@Service
public class DocumentsService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public DocumentsService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    public byte[] generateEnhancedDocx(String submissionId) throws IOException {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for submission ID: " + submissionId);
        }

        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        for (Answer ans : answers) {
            Optional<Question> questionOpt = questionRepository.findById(ans.getQuestionId());
            if (questionOpt.isEmpty() || questionOpt.get().getCategory() == null) continue;

            String category = questionOpt.get().getCategory().getName();
            answersByCategory.computeIfAbsent(category, _ -> new ArrayList<>()).add(ans);
        }

        Map<String, Severity> severities = new HashMap<>();
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            List<AnswerDTO> dtos = entry.getValue().stream()
                    .map(AnswerDTO::new)
                    .collect(Collectors.toList());
            severities.put(entry.getKey(), computeCategorySeverity(dtos));
        }

        try (InputStream template = getClass().getClassLoader().getResourceAsStream("template/template.docx");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (template == null) {
                throw new FileNotFoundException("Template not found in classpath.");
            }

            XWPFDocument document = new XWPFDocument(template);
            Map<String, String> vars = new HashMap<>();
            Answer firstAnswer = answers.getFirst();
            vars.put("submissionId", submissionId);
            vars.put("email", firstAnswer.getEmail());
            vars.put("date", firstAnswer.getCreatedAt().toLocalDate().toString());

            replacePlaceholders(document, vars);
            addSummarySection(document, severities);
            addAnswersTable(document, answersByCategory,severities);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void replacePlaceholders(XWPFDocument doc, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            StringBuilder fullText = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    fullText.append(text);
                }
            }

            String replaced = fullText.toString();
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                replaced = replaced.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            // Clear and replace text
            for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(replaced);
            newRun.setFontFamily("Calibri");
        }
    }

    private void addSummarySection(XWPFDocument document, Map<String, Severity> severities) {
        severities.entrySet().stream()
                .sorted(Map.Entry.<String, Severity>comparingByValue().reversed())
                .forEach(entry -> {
                    XWPFParagraph p = document.createParagraph();
                    XWPFRun run = p.createRun();
                    run.setText(entry.getKey() + ": " + entry.getValue());
                    run.setFontSize(14);
                    run.setFontFamily("Calibri");
                    run.setBold(true);
                });

    }

    private void addAnswersTable(XWPFDocument document, Map<String, List<Answer>> answersByCategory, Map<String, Severity> severities) {
        answersByCategory.entrySet().stream()
                .sorted(Comparator.comparing(
                        (Map.Entry<String, List<Answer>> e) ->
                                Objects.requireNonNullElse(severities.get(e.getKey()), Severity.LOW)
                ).reversed())
                .forEach(entry -> {
                    String category = entry.getKey();
                    List<Answer> answers = entry.getValue();

                    XWPFParagraph categoryHeader = document.createParagraph();
                    XWPFRun headerRun = categoryHeader.createRun();
                    headerRun.setText("Categoria: " + category);
                    headerRun.setBold(true);
                    headerRun.setFontSize(14);
                    headerRun.setFontFamily("Calibri");

                    XWPFTable table = document.createTable();
                    XWPFTableRow header = table.getRow(0);
                    header.getCell(0).setText("Pergunta");
                    header.addNewTableCell().setText("Resposta");
                    header.addNewTableCell().setText("Tipo");
                    header.addNewTableCell().setText("Nível");

                    for (Answer answer : answers) {
                        XWPFTableRow row = table.createRow();
                        row.getCell(0).setText(answer.getQuestionText());
                        row.getCell(1).setText(answer.getUserResponse());
                        row.getCell(2).setText(answer.getQuestionType() != null ? answer.getQuestionType().name() : "-");
                        row.getCell(3).setText(answer.getChosenLevel() != null ? answer.getChosenLevel().name() : "-");
                    }

                    document.createParagraph();
                });
    }
}
