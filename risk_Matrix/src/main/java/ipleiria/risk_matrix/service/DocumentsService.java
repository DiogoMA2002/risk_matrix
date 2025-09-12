package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AnswerDTO;
import ipleiria.risk_matrix.models.answers.Answer;
import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.Question;
import ipleiria.risk_matrix.models.questions.Severity;
import ipleiria.risk_matrix.repository.AnswerRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.*;
import org.springframework.stereotype.Service;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

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

        // Store email for later deletion

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
            byte[] result = out.toByteArray();

            // Delete all answers for this email after generating the document
            answerRepository.deleteAll(answers);

            return result;
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
            newRun.setFontFamily("Verdana");
        }
    }

    private void addSummarySection(XWPFDocument document, Map<String, Severity> severities) {

        // Texto base
        XWPFParagraph summary = document.createParagraph();
        XWPFRun textRun = summary.createRun();
        textRun.setFontSize(11);
        textRun.setFontFamily("Verdana");
        textRun.setText("Este relatório apresenta os resultados da avaliação de risco realizada com base nas respostas submetidas. Foram analisadas várias áreas críticas de segurança da informação, como autenticação, backups, rede e acesso remoto. Abaixo apresenta-se um resumo das categorias avaliadas e os respetivos níveis de risco atribuídos:");

        // Add pie chart
        try {
            addPieChart(document, severities);
        } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            // Log error but continue with document generation
            System.err.println("Error creating pie chart: " + e.getMessage());
        }
        addPageBreak(document);
        // Lista de categorias com severidade
        XWPFParagraph categoryListHeader = document.createParagraph();
        XWPFRun headerRun = categoryListHeader.createRun();
        
        severities.entrySet().stream()
                .sorted(Map.Entry.<String, Severity>comparingByValue().reversed())
                .forEach(entry -> {
                    XWPFParagraph p = document.createParagraph();
                    XWPFRun categoryRun = p.createRun();
                    categoryRun.setText("- " + entry.getKey() + ": " + entry.getValue());
                    categoryRun.setFontSize(12);
                    categoryRun.setFontFamily("Verdana");

                    switch (entry.getValue()) {
                        case CRITICAL -> categoryRun.setColor("8B0000");
                        case HIGH -> categoryRun.setColor("FF0000");
                        case MEDIUM -> categoryRun.setColor("FFA500");
                        case LOW -> categoryRun.setColor("008000");
                        case UNKNOWN -> categoryRun.setColor("808080");
                    }
                });

        // Conclusão
        XWPFParagraph outro = document.createParagraph();
        XWPFRun outroRun = outro.createRun();
        outroRun.setFontSize(11);
        outroRun.setFontFamily("Verdana");
        outroRun.setText("Recomenda-se a priorização das categorias com risco mais elevado. As recomendações específicas estão detalhadas por domínio no relatório abaixo, e visam mitigar vulnerabilidades identificadas com base em boas práticas de cibersegurança adaptadas.");
        addPageBreak(document);
    }

    private void addPieChart(XWPFDocument doc, Map<String, Severity> severities)
            throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {

        // Count per severity and compute percentages
        Map<String, Long> counts = severities.values().stream()
                .collect(Collectors.groupingBy(this::getSeverityDisplayName,
                        LinkedHashMap::new, Collectors.counting()));
        long total = Math.max(1, counts.values().stream().mapToLong(Long::longValue).sum());

        // Labels with percentages for the legend
        List<String> labels = new ArrayList<>(counts.keySet());
        List<String> labelsWithPct = labels.stream()
                .map(lbl -> {
                    long c = counts.get(lbl);
                    double pct = 100.0 * c / total;
                    return String.format("%s (%.0f%%)", lbl, pct);
                })
                .toList();

        Double[] values = counts.values().stream().map(Long::doubleValue).toArray(Double[]::new);

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = title.createRun();
        r.setBold(true);
        r.setFontFamily("Calibri");
        r.setFontSize(14);
        r.setText("Distribuição por Categoria");

        // Size to practical full-page width: ~17.5 cm wide, 10.5 cm high
        final int W = (int) (17.5 * Units.EMU_PER_CENTIMETER);
        final int H = (int) (10.5 * Units.EMU_PER_CENTIMETER);
        XWPFChart chart = doc.createChart(W, H);

        // White background for chart and plot area
        setChartBackgroundWhite(chart);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        XDDFDataSource<String> dsLabels = XDDFDataSourcesFactory.fromArray(labelsWithPct.toArray(String[]::new));
        XDDFNumericalDataSource<Double> dsValues = XDDFDataSourcesFactory.fromArray(values);

        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(dsLabels, dsValues);
        series.setShowLeaderLines(true);
        data.setVaryColors(false); // we colour each slice ourselves
        chart.plot(data);

        // Data labels on slices: show percentages only
        CTPieChart ctPie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDLbls dLbls = ctPie.isSetDLbls() ? ctPie.getDLbls() : ctPie.addNewDLbls();
        dLbls.addNewShowLegendKey().setVal(false);
        dLbls.addNewShowVal().setVal(false);
        dLbls.addNewShowCatName().setVal(false);
        dLbls.addNewShowSerName().setVal(false);
        dLbls.addNewShowPercent().setVal(true);

        // Severity colours per slice (series index 0)
        for (int i = 0; i < labels.size(); i++) {
            int[] rgb = colourForSeverityLabel(labels.get(i)); // use base label to pick colour
            setPieSliceRgb(chart, 0, i, rgb[0], rgb[1], rgb[2]);
        }
    }

    // Solid fill helper for any shape properties
    private void setSolidFillRgb(CTShapeProperties spPr, int r, int g, int b) {
        CTSolidColorFillProperties solid = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
        CTSRgbColor rgb = solid.isSetSrgbClr() ? solid.getSrgbClr() : solid.addNewSrgbClr();
        rgb.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
    }
    // Map severity label -> colour
    private int[] colourForSeverityLabel(String label) {
        return switch (label) {
            case "Crítico" -> new int[]{139, 0, 0};     // dark red
            case "Alto"    -> new int[]{255, 0, 0};     // red
            case "Médio"   -> new int[]{255, 165, 0};   // orange
            case "Baixo"   -> new int[]{0, 128, 0};     // green
            default        -> new int[]{128, 128, 128}; // grey
        };
    }
    private void addPageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setPageBreak(true);
    }
    // White background for chart and plot area
    private void setChartBackgroundWhite(XWPFChart chart) {
        // Chart space
        CTChartSpace cs = chart.getCTChartSpace();
        CTShapeProperties spPr = cs.isSetSpPr() ? cs.getSpPr() : cs.addNewSpPr();
        setSolidFillRgb(spPr, 255, 255, 255);

        // Plot area
        CTPlotArea pa = chart.getCTChart().getPlotArea();
        CTShapeProperties plotSpPr = pa.isSetSpPr() ? pa.getSpPr() : pa.addNewSpPr();
        setSolidFillRgb(plotSpPr, 255, 255, 255);
    }



    private String getSeverityDisplayName(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "Crítico";
            case HIGH -> "Alto";
            case MEDIUM -> "Médio";
            case LOW -> "Baixo";
            case UNKNOWN -> "Desconhecido";
        };
    }
    private void setPieSliceRgb(XWPFChart chart, int seriesIdx, int pointIdx, int r, int g, int b) {
        CTPieChart pie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDPt dpt = pie.getSerArray(seriesIdx).addNewDPt();
        dpt.addNewIdx().setVal(pointIdx);

        CTShapeProperties spPr = dpt.isSetSpPr() ? dpt.getSpPr() : dpt.addNewSpPr();
        CTSolidColorFillProperties solid = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
        if (solid.isSetSrgbClr()) solid.setSrgbClr(rgb); else solid.addNewSrgbClr().set(rgb);
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
                    Severity categorySeverity = severities.get(category);

                    XWPFParagraph categoryHeader = document.createParagraph();
                    XWPFRun headerRun = categoryHeader.createRun();
                    headerRun.setText("Categoria: " + category);
                    headerRun.setBold(true);
                    headerRun.setFontSize(14);
                    headerRun.setFontFamily("Calibri");
                    
                    // Color the category header based on severity
                    switch (categorySeverity) {
                        case Severity.CRITICAL:
                            headerRun.setColor("8B0000"); // Dark Red
                            break;
                        case Severity.HIGH:
                            headerRun.setColor("FF0000"); // Red
                            break;
                        case Severity.MEDIUM:
                            headerRun.setColor("FFA500"); // Orange
                            break;
                        case Severity.LOW:
                            headerRun.setColor("008000"); // Green
                            break;
                        case Severity.UNKNOWN:
                            headerRun.setColor("808080"); // Gray
                            break;
                        default:
                            headerRun.setColor("000000"); // Black
                    }

                    XWPFTable table = document.createTable();
                    XWPFTableRow header = table.getRow(0);
                    header.getCell(0).setText("Pergunta");
                    header.addNewTableCell().setText("Resposta");
                    header.addNewTableCell().setText("Tipo");
                    header.addNewTableCell().setText("Nível");
                    header.addNewTableCell().setText("Recomendação");

                    for (Answer answer : answers.stream()
                            .sorted(Comparator.comparing(
                                    a -> Optional.ofNullable(a.getChosenLevel()).orElse(OptionLevel.LOW),
                                    Comparator.reverseOrder()
                            ))
                            .toList()) {

                        XWPFTableRow row = table.createRow();
                        row.getCell(0).setText(answer.getQuestionText());
                        row.getCell(1).setText(answer.getUserResponse());
                        row.getCell(2).setText(answer.getQuestionType() != null ? answer.getQuestionType().name() : "-");
                        
                        // Color the level cell based on the chosen level
                        XWPFTableCell levelCell = row.getCell(3);
                        levelCell.setText(answer.getChosenLevel() != null ? answer.getChosenLevel().name() : "-");
                        if (answer.getChosenLevel() != null) {
                            switch (answer.getChosenLevel()) {
                                case OptionLevel.HIGH:
                                    levelCell.setColor("FF0000"); // Red
                                    break;
                                case OptionLevel.MEDIUM:
                                    levelCell.setColor("FFA500"); // Orange
                                    break;
                                case OptionLevel.LOW:
                                    levelCell.setColor("008000"); // Green
                                    break;
                            }
                        }
                        
                        String recommendation = "-";
                        if (answer.getQuestionOptionId() != null) {
                            Optional<Question> questionOpt = questionRepository.findById(answer.getQuestionId());
                            if (questionOpt.isPresent()) {
                                recommendation = questionOpt.get().getOptions().stream()
                                        .filter(opt -> opt.getId().equals(answer.getQuestionOptionId()))
                                        .map(opt -> opt.getRecommendation() != null ? opt.getRecommendation() : "-")
                                        .findFirst()
                                        .orElse("-");
                            }
                        }
                        row.getCell(4).setText(recommendation);
                    }
                    document.createParagraph();
                });
    }
}
