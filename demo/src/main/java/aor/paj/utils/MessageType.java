package aor.paj.utils;

public enum MessageType {
    MESSAGE_RECEIVER(10),
    MESSAGE_SENDER(11),
    TASK_CREATE(21),
    TASK_MOVE(22),
    TASK_EDIT(23),
    TASK_EDIT_AND_MOVE(24),
    TASK_DESACTIVATE(25),
    TASK_DELETE(26),
    LOGOUT(30),
    STATISTIC_USER(31),
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