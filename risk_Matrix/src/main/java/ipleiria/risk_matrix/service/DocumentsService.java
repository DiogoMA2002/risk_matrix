package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public byte[] generateEnhancedDocx(String submissionId) throws IOException, InvalidFormatException {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for submission ID: " + submissionId);
        }

        // Group answers by category for summary and compute severity per category.
        Map<String, List<Answer>> answersByCategory = new HashMap<>();
        for (Answer ans : answers) {
            // Retrieve question info per answer; skip if missing category.
            Question question = questionRepository.findById(ans.getQuestionId()).orElse(null);
            if (question == null || question.getCategory() == null) continue;
            String category = question.getCategory().getName();
            answersByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(ans);
        }

        Map<String, Severity> severities = new HashMap<>();
        for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
            List<AnswerDTO> dtos = entry.getValue().stream()
                    .map(AnswerDTO::new)
                    .collect(Collectors.toList());
            severities.put(entry.getKey(), computeCategorySeverity(dtos));
        }

        XWPFDocument document = new XWPFDocument();
        // Path to the cover image.
        String imagePath = "src/main/resources/images/capa.png";

        // Note: no caching for answers.get(0) is done—using it directly.
        addCoverPage(document, submissionId, answers.get(0), imagePath);
        addSubmissionInfo(document, submissionId, answers.get(0));
        addSummarySection(document, severities);
        addAnswersTable(document, answers);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        return out.toByteArray();
    }

    /**
     * Adds a dedicated cover page with a title and a full-page (nearly) cover image.
     */

    private void addCoverPage(XWPFDocument document, String submissionId, Answer firstAnswer, String imagePath)
            throws InvalidFormatException, IOException {
        // Optionally set minimal or zero margins for the entire section (A4 in portrait).
        // This helps the image truly fill the page.
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        // For "borderless" margins, set them to 0 or a very small value (in twentieths of a point).
        pageMar.setTop(BigInteger.valueOf(300));     // ~0.21 cm
        pageMar.setBottom(BigInteger.valueOf(300));
        pageMar.setLeft(BigInteger.valueOf(300));
        pageMar.setRight(BigInteger.valueOf(300));

        // -- 1. Paragraph for the Title (no page break).
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("RELATÓRIO DE RESPOSTAS");
        titleRun.setBold(true);
        titleRun.setFontSize(24);

        // -- 2. Paragraph for the cover image (still on the same page).
        XWPFParagraph coverParagraph = document.createParagraph();
        coverParagraph.setAlignment(ParagraphAlignment.CENTER);
        // Make sure not to set pageBreak(true) here.
        XWPFRun coverRun = coverParagraph.createRun();

        // Slightly reduce the image size so it fits on the same page with the title above.
        // 595×842 EMUs is exactly A4, but you must allow some space for margins & the title paragraph.
        try (InputStream imageStream = new FileInputStream(imagePath)) {
            coverRun.addPicture(
                    imageStream,
                    Document.PICTURE_TYPE_PNG,
                    imagePath,
                    Units.toEMU(595), // Adjust width
                    Units.toEMU(750)  // Adjust height so there's room for the title
            );
        }

        // -- 3. Metadata below the image, still on the same page.
        XWPFParagraph meta = document.createParagraph();
        meta.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun metaRun = meta.createRun();
        metaRun.addBreak();
        metaRun.setText("Submission ID: " + submissionId);
        metaRun.addBreak();
        metaRun.setText("Email: " + firstAnswer.getEmail());
        metaRun.addBreak();
        metaRun.setText("Data: " + firstAnswer.getCreatedAt().toLocalDate());

        // -- 4. Now insert an explicit page break to move on to the next page.
        XWPFParagraph pageBreakParagraph = document.createParagraph();
        pageBreakParagraph.setPageBreak(true);
    }


    /**
     * Adds submission information on the following page.
     */
    private void addSubmissionInfo(XWPFDocument document, String submissionId, Answer firstAnswer) {
        XWPFParagraph info = document.createParagraph();
        XWPFRun infoRun = info.createRun();
        infoRun.setText("Submission ID: " + submissionId);
        infoRun.addBreak();
        infoRun.setText("Email: " + firstAnswer.getEmail());
        infoRun.addBreak();
        infoRun.setText("Data: " + firstAnswer.getCreatedAt().toLocalDate());
    }

    /**
     * Adds a summary section listing the computed severity per category.
     */
    private void addSummarySection(XWPFDocument document, Map<String, Severity> severities) {
        XWPFParagraph summaryTitle = document.createParagraph();
        summaryTitle.setSpacingBefore(500);
        XWPFRun runSummary = summaryTitle.createRun();
        runSummary.setText("Resumo por Categoria de Risco");
        runSummary.setBold(true);
        runSummary.setFontSize(16);

        for (Map.Entry<String, Severity> entry : severities.entrySet()) {
            XWPFParagraph p = document.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(entry.getKey() + ": " + entry.getValue());
            run.setFontSize(14);
            run.setBold(true);
        }
    }

    /**
     * Creates a table that lists each answer with its associated question, response, type, and chosen level.
     */
    private void addAnswersTable(XWPFDocument document, List<Answer> answers) {
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
    }

}
