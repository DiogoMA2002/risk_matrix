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
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for submission ID: " + submissionId);
        }

        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        for (Answer ans : answers) {
            Question question = questionRepository.findById(ans.getQuestionId()).orElse(null);
            if (question == null || question.getCategory() == null) continue;
            String category = question.getCategory().getName();
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
            vars.put("submissionId", submissionId);
            vars.put("email", answers.getFirst().getEmail());
            vars.put("date", answers.getFirst().getCreatedAt().toLocalDate().toString());

            replacePlaceholders(document, vars);

            // Add dynamic content after cover page
            addSummarySection(document, severities);
            addAnswersTable(document, answersByCategory);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void replacePlaceholders(XWPFDocument doc, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            StringBuilder fullText = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                fullText.append(run.getText(0));
            }

            String replaced = fullText.toString();
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                replaced = replaced.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            // Clear existing runs
            int numRuns = paragraph.getRuns().size();
            for (int i = numRuns - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            // Create new run with replaced text
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(replaced);
        }
    }

    private void addSummarySection(XWPFDocument document, Map<String, Severity> severities) {

        for (Map.Entry<String, Severity> entry : severities.entrySet()) {
            XWPFParagraph p = document.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(entry.getKey() + ": " + entry.getValue());
            run.setFontSize(14);
            run.setBold(true);
        }
    }

    private void addAnswersTable(XWPFDocument document, Map<String, List<Answer>> answersByCategory) {
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            List<Answer> answers = entry.getValue();

            // Add category title
            XWPFParagraph categoryHeader = document.createParagraph();
            XWPFRun headerRun = categoryHeader.createRun();
            headerRun.setText("Categoria: " + category);
            headerRun.setBold(true);
            headerRun.setFontSize(14);

            // Create table
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

            // Add spacing between tables
            document.createParagraph();
        }
    }

}
