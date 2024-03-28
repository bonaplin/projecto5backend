package aor.paj.utils;

public enum Priority {
    LOW(100), MEDIUM(200), HIGH(300);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
