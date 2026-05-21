package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.questions.Severity;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ipleiria.risk_matrix.utils.documents.ReportPresentation.*;

public final class DocumentChartBuilder {

    private DocumentChartBuilder() {}

    public static void addSeverityPieChart(XWPFDocument document, Map<String, Severity> severities)
            throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {

        Map<Severity, Long> countsBySeverity = severities.values().stream()
                .collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.counting()));
        long total = Math.max(1, countsBySeverity.values().stream().mapToLong(Long::longValue).sum());

        List<Severity> severityKeys = new ArrayList<>(countsBySeverity.keySet());
        List<String> labelsWithPct = severityKeys.stream()
                .map(s -> {
                    long count = countsBySeverity.get(s);
                    double pct = 100.0 * count / total;
                    return String.format("%s (%.0f%%)", severityDisplayName(s), pct);
                })
                .toList();
        Double[] values = severityKeys.stream()
                .map(s -> countsBySeverity.get(s).doubleValue())
                .toArray(Double[]::new);

        addChartTitle(document, "Distribuição por Nível de Severidade");

        XWPFChart chart = createChart(document, 17.5, 10.5);
        setChartBackgroundWhite(chart);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        XDDFDataSource<String> labels = XDDFDataSourcesFactory.fromArray(labelsWithPct.toArray(String[]::new));
        XDDFNumericalDataSource<Double> dataValues = XDDFDataSourcesFactory.fromArray(values);

        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(labels, dataValues);
        series.setShowLeaderLines(true);
        data.setVaryColors(false);
        chart.plot(data);

        configurePieLabels(chart);
        for (int i = 0; i < severityKeys.size(); i++) {
            int[] rgb = severityRgb(severityKeys.get(i));
            setPieSliceRgb(chart, i, rgb[0], rgb[1], rgb[2]);
        }
    }

    public static void addCategoryScoreBarChart(XWPFDocument document, Map<String, Integer> categoryScores)
            throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {

        List<Map.Entry<String, Integer>> ranked = categoryScores.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        if (ranked.isEmpty()) {
            return;
        }

        addChartTitle(document, "Pontuação por Categoria");

        XWPFChart chart = createChart(document, 17.5, 10.5);
        setChartBackgroundWhite(chart);

        String[] categories = ranked.stream().map(Map.Entry::getKey).toArray(String[]::new);
        Double[] scores = ranked.stream().map(e -> e.getValue().doubleValue()).toArray(Double[]::new);

        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        categoryAxis.setTitle("Categoria");
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setTitle("Pontuação (1–9)");
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        valueAxis.setMinimum(0d);
        valueAxis.setMaximum(9d);

        XDDFDataSource<String> labels = XDDFDataSourcesFactory.fromArray(categories);
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromArray(scores);

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
        data.setBarDirection(BarDirection.COL);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(labels, values);
        series.setTitle("Pontuação", null);
        chart.plot(data);

        colorBarPoints(chart, ranked);
    }

    private static void addChartTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontFamily(FONT);
        run.setFontSize(14);
        run.setText(title);
    }

    private static XWPFChart createChart(XWPFDocument document, double widthCm, double heightCm)
            throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        int width = (int) (widthCm * Units.EMU_PER_CENTIMETER);
        int height = (int) (heightCm * Units.EMU_PER_CENTIMETER);
        return document.createChart(width, height);
    }

    private static void configurePieLabels(XWPFChart chart) {
        CTPieChart ctPie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDLbls dLbls = ctPie.isSetDLbls() ? ctPie.getDLbls() : ctPie.addNewDLbls();
        dLbls.addNewShowLegendKey().setVal(false);
        dLbls.addNewShowVal().setVal(false);
        dLbls.addNewShowCatName().setVal(false);
        dLbls.addNewShowSerName().setVal(false);
        dLbls.addNewShowPercent().setVal(true);
    }

    private static void colorBarPoints(XWPFChart chart, List<Map.Entry<String, Integer>> ranked) {
        CTBarChart barChart = chart.getCTChart().getPlotArea().getBarChartArray(0);
        for (int i = 0; i < ranked.size(); i++) {
            int score = ranked.get(i).getValue();
            int[] rgb = hexToRgb(scoreColorHex(score));
            setBarPointRgb(chart, i, rgb[0], rgb[1], rgb[2]);
        }
    }

    private static int[] hexToRgb(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static void setChartBackgroundWhite(XWPFChart chart) {
        CTChartSpace chartSpace = chart.getCTChartSpace();
        CTShapeProperties spaceProps = chartSpace.isSetSpPr() ? chartSpace.getSpPr() : chartSpace.addNewSpPr();
        setSolidFillWhite(spaceProps);

        CTPlotArea plotArea = chart.getCTChart().getPlotArea();
        CTShapeProperties plotProps = plotArea.isSetSpPr() ? plotArea.getSpPr() : plotArea.addNewSpPr();
        setSolidFillWhite(plotProps);
    }

    private static void setSolidFillWhite(CTShapeProperties shapeProperties) {
        CTSolidColorFillProperties solid = shapeProperties.isSetSolidFill()
                ? shapeProperties.getSolidFill()
                : shapeProperties.addNewSolidFill();
        CTSRgbColor rgb = solid.isSetSrgbClr() ? solid.getSrgbClr() : solid.addNewSrgbClr();
        rgb.setVal(new byte[]{(byte) 255, (byte) 255, (byte) 255});
    }

    private static void setPieSliceRgb(XWPFChart chart, int pointIdx, int r, int g, int b) {
        CTPieChart pie = chart.getCTChart().getPlotArea().getPieChartArray(0);
        CTDPt point = pie.getSerArray(0).addNewDPt();
        point.addNewIdx().setVal(pointIdx);
        applyPointRgb(point, r, g, b);
    }

    private static void setBarPointRgb(XWPFChart chart, int pointIdx, int r, int g, int b) {
        CTBarChart bar = chart.getCTChart().getPlotArea().getBarChartArray(0);
        CTDPt point = bar.getSerArray(0).addNewDPt();
        point.addNewIdx().setVal(pointIdx);
        applyPointRgb(point, r, g, b);
    }

    private static void applyPointRgb(CTDPt point, int r, int g, int b) {
        CTShapeProperties shapeProperties = point.isSetSpPr() ? point.getSpPr() : point.addNewSpPr();
        CTSolidColorFillProperties solid = shapeProperties.isSetSolidFill()
                ? shapeProperties.getSolidFill()
                : shapeProperties.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
        if (solid.isSetSrgbClr()) {
            solid.setSrgbClr(rgb);
        } else {
            solid.addNewSrgbClr().set(rgb);
        }
    }
}
