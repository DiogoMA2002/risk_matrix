package ipleiria.risk_matrix.models.questions;

public enum OptionLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int value;

    OptionLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OptionLevel fromValue(int val) {
        return switch (val) {
            case 1 -> LOW;
            case 2 -> MEDIUM;
            case 3 -> HIGH;
            default -> null;
        };
    }
}
