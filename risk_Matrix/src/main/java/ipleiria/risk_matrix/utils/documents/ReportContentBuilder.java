package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import org.apache.poi.xwpf.usermodel.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static ipleiria.risk_matrix.utils.documents.ReportPresentation.*;

public final class ReportContentBuilder {

    private ReportContentBuilder() {}

    public static void addPageBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setPageBreak(true);
    }

    public static void addSkippedAnswersSection(XWPFDocument document, List<SkippedAnswer> skippedAnswers) {
        if (skippedAnswers.isEmpty()) {
            return;
        }

        XWPFParagraph title = document.createParagraph();
        XWPFRun titleRun = title.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(12);
        titleRun.setFontFamily(FONT);
        titleRun.setColor("8B0000");
        titleRun.setText("Respostas não incluídas no relatório");

        XWPFParagraph intro = document.createParagraph();
        XWPFRun introRun = intro.createRun();
        introRun.setFontSize(10);
        introRun.setFontFamily(FONT);
        introRun.setText("As seguintes respostas não puderam ser agrupadas por categoria e foram excluídas das secções analíticas:");

        for (SkippedAnswer skipped : skippedAnswers) {
            XWPFParagraph item = document.createParagraph();
            XWPFRun itemRun = item.createRun();
            itemRun.setFontSize(10);
            itemRun.setFontFamily(FONT);
            itemRun.setText(String.format("- Pergunta ID %d (%s): %s",
                    skipped.questionId(),
                    skipped.questionText(),
                    skipped.reason()));
        }

        document.createParagraph();
    }

    public static void addSummaryIntro(XWPFDocument document) {
        XWPFParagraph summary = document.createParagraph();
        XWPFRun textRun = summary.createRun();
        textRun.setFontSize(11);
        textRun.setFontFamily(FONT);
        textRun.setText("Este relatório apresenta os resultados da avaliação de risco realizada com base nas respostas submetidas. Foram analisadas várias áreas críticas de segurança da informação, como autenticação, backups, rede e acesso remoto. Abaixo apresenta-se um resumo das categorias avaliadas e os respetivos níveis de risco atribuídos:");
    }

    public static void addSummarySeverityList(XWPFDocument document, Map<String, Severity> severities) {
        severities.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Severity>>comparingInt(e -> severityLevel(e.getValue())).reversed())
                .forEach(entry -> {
                    XWPFParagraph paragraph = document.createParagraph();

                    XWPFRun categoryRun = paragraph.createRun();
                    categoryRun.setFontSize(12);
                    categoryRun.setFontFamily(FONT);
                    categoryRun.setText("- " + entry.getKey() + ": ");

                    Severity severity = entry.getValue();
                    int level = severityLevel(severity);
                    XWPFRun severityRun = paragraph.createRun();
                    severityRun.setFontSize(12);
                    severityRun.setFontFamily(FONT);
                    severityRun.setBold(true);
                    severityRun.setColor(severityColorHex(severity));
                    if (level > 0) {
                        severityRun.setText(severityDisplayName(severity) + " (" + level + ")");
                    } else {
                        severityRun.setText(severityDisplayName(severity));
                    }
                });

        XWPFParagraph levelExplanation = document.createParagraph();
        XWPFRun explanationRun = levelExplanation.createRun();
        explanationRun.setFontSize(10);
        explanationRun.setFontFamily(FONT);
        explanationRun.setItalic(true);
        explanationRun.setText("Nota: Os níveis numéricos entre parênteses referem-se à criticidade do risco: (1) = Baixo, (2) = Médio, (3) = Alto, (4) = Crítico.");

        XWPFParagraph outro = document.createParagraph();
        XWPFRun outroRun = outro.createRun();
        outroRun.setFontSize(11);
        outroRun.setFontFamily(FONT);
        outroRun.setText("Recomenda-se a priorização das categorias com risco mais elevado. As recomendações específicas estão detalhadas por domínio no relatório abaixo, e visam mitigar vulnerabilidades identificadas com base em boas práticas de cibersegurança adaptadas.");

        addPageBreak(document);
    }

    public static void addQuantitativeSection(XWPFDocument document,
                                              Map<String, Integer> categoryScores,
                                              double globalIndex,
                                              Map<String, Severity> severities) {
        XWPFParagraph titlePara = document.createParagraph();
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        titleRun.setFontFamily(FONT);
        titleRun.setText("Análise Quantitativa");

        boolean hasValidScores = categoryScores.values().stream().anyMatch(score -> score > 0);

        XWPFParagraph indexPara = document.createParagraph();
        XWPFRun labelRun = indexPara.createRun();
        labelRun.setFontSize(12);
        labelRun.setFontFamily(FONT);
        labelRun.setText("Índice de Risco Global: ");

        XWPFRun indexRun = indexPara.createRun();
        indexRun.setBold(true);
        indexRun.setFontSize(12);
        indexRun.setFontFamily(FONT);
        if (hasValidScores) {
            indexRun.setText(String.format("%.0f / 100", globalIndex));
            indexRun.setColor(indexColorHex(globalIndex));
        } else {
            indexRun.setText("N/D");
            indexRun.setColor("808080");
        }

        XWPFParagraph notePara = document.createParagraph();
        XWPFRun noteRun = notePara.createRun();
        noteRun.setFontSize(9);
        noteRun.setFontFamily(FONT);
        noteRun.setItalic(true);
        noteRun.setText("Nota: A pontuação por categoria (1–9) é o produto das medianas de impacto e probabilidade. O índice global (0–100) é a média normalizada das pontuações válidas; categorias sem dados suficientes são excluídas do cálculo (DESCONHECIDO).");

        document.createParagraph();

        XWPFTable table = document.createTable();
        styleTableHeader(table.getRow(0), "Categoria", "Pontuação (1–9)", "Índice (%)", "Severidade");

        categoryScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String category = entry.getKey();
                    int score = entry.getValue();
                    Severity severity = severities.getOrDefault(category, Severity.UNKNOWN);

                    XWPFTableRow row = table.createRow();
                    row.getCell(0).setText(category);

                    XWPFTableCell scoreCell = row.getCell(1);
                    XWPFTableCell indexCell = row.getCell(2);
                    if (score > 0) {
                        scoreCell.setText(String.valueOf(score));
                        scoreCell.setColor(scoreColorHex(score));
                        indexCell.setText(String.format("%.0f%%", normalizedScorePercent(score)));
                        indexCell.setColor(scoreColorHex(score));
                    } else {
                        scoreCell.setText("-");
                        indexCell.setText("-");
                    }

                    XWPFTableCell severityCell = row.getCell(3);
                    severityCell.setText(severityDisplayName(severity));
                    severityCell.setColor(severityColorHex(severity));
                });
    }

    public static void addAnswersTable(XWPFDocument document,
                                       Map<String, List<Answer>> answersByCategory,
                                       Map<String, Severity> severities,
                                       Map<Long, Question> questionMap) {
        final boolean[] first = {true};

        answersByCategory.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<Answer>>>comparingInt(e ->
                        severityLevel(severities.getOrDefault(e.getKey(), Severity.UNKNOWN))).reversed())
                .forEach(entry -> {
                    if (!first[0]) {
                        document.createParagraph().setPageBreak(true);
                    }
                    first[0] = false;

                    String category = entry.getKey();
                    List<Answer> answers = entry.getValue();
                    Severity categorySeverity = severities.getOrDefault(category, Severity.UNKNOWN);

                    XWPFParagraph categoryHeader = document.createParagraph();
                    XWPFRun headerRun = categoryHeader.createRun();
                    int level = severityLevel(categorySeverity);
                    String severityText = severityDisplayName(categorySeverity);
                    headerRun.setText(level > 0
                            ? "Categoria: " + category + " - " + severityText + " (" + level + ")"
                            : "Categoria: " + category + " - " + severityText);
                    headerRun.setBold(true);
                    headerRun.setFontSize(14);
                    headerRun.setFontFamily(FONT);
                    headerRun.setColor(severityColorHex(categorySeverity));

                    XWPFTable table = document.createTable();
                    styleTableHeader(table.getRow(0), "Pergunta", "Resposta", "Tipo", "Nível", "Recomendação");

                    answers.stream()
                            .sorted(Comparator.comparing(Answer::getQuestionId))
                            .forEach(answer -> {
                                XWPFTableRow row = table.createRow();
                                row.getCell(0).setText(answer.getQuestionText());
                                row.getCell(1).setText(answer.getUserResponse());
                                row.getCell(2).setText(answer.getQuestionType() != null
                                        ? answer.getQuestionType().name()
                                        : "-");

                                XWPFTableCell levelCell = row.getCell(3);
                                levelCell.setText(optionLevelDisplayName(answer.getChosenLevel()));
                                if (answer.getChosenLevel() != null) {
                                    levelCell.setColor(optionLevelColorHex(answer.getChosenLevel()));
                                }

                                Question question = questionMap.get(answer.getQuestionId());
                                row.getCell(4).setText(ReportRecommendationResolver.resolve(answer, question));
                            });

                    document.createParagraph();
                });
    }

    private static void styleTableHeader(XWPFTableRow headerRow, String... titles) {
        for (int i = 0; i < titles.length; i++) {
            XWPFTableCell cell = i == 0 ? headerRow.getCell(0) : headerRow.addNewTableCell();
            cell.setText(titles[i]);
            cell.getParagraphs().forEach(paragraph ->
                    paragraph.getRuns().forEach(run -> {
                        run.setBold(true);
                        run.setFontFamily(FONT);
                    }));
        }
    }
}
