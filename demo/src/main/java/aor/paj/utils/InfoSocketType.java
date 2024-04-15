package aor.paj.utils;

public enum InfoSocketType {
    LOGOUT(30),
    TYPE_31(31),
    TYPE_32(32),
    TYPE_33(33),
    TYPE_34(34);

    private final int value;

    InfoSocketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}