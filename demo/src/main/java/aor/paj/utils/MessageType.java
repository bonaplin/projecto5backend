package aor.paj.utils;

public enum MessageType {
    TYPE_10(10),
    TYPE_20(20),
    LOGOUT(30),
    TYPE_31(31),
    TYPE_32(32),
    TYPE_33(33),
    TYPE_34(34),
    TYPE_40(40);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType type : MessageType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid message type value: " + value);
    }
}