package ipleiria.risk_matrix.utils.documents;

import ipleiria.risk_matrix.models.questions.OptionLevel;
import ipleiria.risk_matrix.models.questions.Severity;

public final class ReportPresentation {

    public static final String FONT = "Verdana";

    private ReportPresentation() {}

    public static String severityDisplayName(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "Crítico";
            case HIGH     -> "Alto";
            case MEDIUM   -> "Médio";
            case LOW      -> "Baixo";
            case UNKNOWN  -> "Desconhecido";
        };
    }

    public static int severityLevel(Severity severity) {
        return switch (severity) {
            case CRITICAL -> 4;
            case HIGH     -> 3;
            case MEDIUM   -> 2;
            case LOW      -> 1;
            case UNKNOWN  -> 0;
        };
    }

    public static String severityColorHex(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "8B0000";
            case HIGH     -> "FF0000";
            case MEDIUM   -> "FFA500";
            case LOW      -> "008000";
            case UNKNOWN  -> "808080";
        };
    }

    public static int[] severityRgb(Severity severity) {
        return switch (severity) {
            case CRITICAL -> new int[]{139, 0, 0};
            case HIGH     -> new int[]{255, 0, 0};
            case MEDIUM   -> new int[]{255, 165, 0};
            case LOW      -> new int[]{0, 128, 0};
            case UNKNOWN  -> new int[]{128, 128, 128};
        };
    }

    public static String optionLevelDisplayName(OptionLevel level) {
        if (level == null) {
            return "-";
        }
        return switch (level) {
            case HIGH   -> "ALTO";
            case MEDIUM -> "MÉDIO";
            case LOW    -> "BAIXO";
        };
    }

    public static String optionLevelColorHex(OptionLevel level) {
        if (level == null) {
            return "808080";
        }
        return switch (level) {
            case HIGH   -> "FF0000";
            case MEDIUM -> "FFA500";
            case LOW    -> "008000";
        };
    }

    public static String scoreColorHex(int score) {
        if (score <= 2) return "008000";
        if (score <= 4) return "FFA500";
        if (score <= 6) return "FF0000";
        return "8B0000";
    }

    public static String indexColorHex(double index) {
        if (index >= 88) return "8B0000";
        if (index >= 63) return "FF0000";
        if (index >= 38) return "FFA500";
        return "008000";
    }

    public static double normalizedScorePercent(int score) {
        return (score - 1.0) / 8.0 * 100.0;
    }
}
