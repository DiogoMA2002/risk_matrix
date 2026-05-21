package ipleiria.risk_matrix.utils.documents;

import org.apache.poi.xwpf.usermodel.*;

import java.util.List;
import java.util.Map;

public final class DocumentPlaceholderReplacer {

    private DocumentPlaceholderReplacer() {}

    public static void replaceAll(XWPFDocument document, Map<String, String> replacements) {
        replaceInParagraphs(document.getParagraphs(), replacements);

        for (XWPFTable table : document.getTables()) {
            replaceInTable(table, replacements);
        }

        for (XWPFHeader header : document.getHeaderList()) {
            replaceInParagraphs(header.getParagraphs(), replacements);
            for (XWPFTable table : header.getTables()) {
                replaceInTable(table, replacements);
            }
        }

        for (XWPFFooter footer : document.getFooterList()) {
            replaceInParagraphs(footer.getParagraphs(), replacements);
            for (XWPFTable table : footer.getTables()) {
                replaceInTable(table, replacements);
            }
        }
    }

    private static void replaceInTable(XWPFTable table, Map<String, String> replacements) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                replaceInParagraphs(cell.getParagraphs(), replacements);
                for (XWPFTable nested : cell.getTables()) {
                    replaceInTable(nested, replacements);
                }
            }
        }
    }

    private static void replaceInParagraphs(List<XWPFParagraph> paragraphs, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : paragraphs) {
            replaceInParagraph(paragraph, replacements);
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> replacements) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) {
            return;
        }

        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }
        }

        String original = fullText.toString();
        String replaced = original;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            replaced = replaced.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        if (original.equals(replaced)) {
            return;
        }

        runs.getFirst().setText(replaced, 0);
        for (int i = runs.size() - 1; i >= 1; i--) {
            paragraph.removeRun(i);
        }
    }
}
