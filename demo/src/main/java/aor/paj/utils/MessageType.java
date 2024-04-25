package aor.paj.utils;

public enum MessageType {
    MESSAGE_RECEIVER(10),
    MESSAGE_SENDER(11),
    MESSAGE_READ(12),
    MESSAGE_READ_CONFIRMATION(13),
    MESSAGE_MARK_AS_READ(14),
    TASK_CREATE(21),
    TASK_MOVE(22),
    TASK_EDIT(23),
    TASK_EDIT_AND_MOVE(24),
    TASK_DESACTIVATE(25),
    TASK_DELETE(26),
    TASK_RESTORE(27),
    TASK_RESTORE_ALL(28),
    TASK_DELETE_ALL(29),
    LOGOUT(30),
    STATISTIC_USER(31),
    STATISTIC_TASK(32),
    STATISTIC_TASK_PER_STATUS(33),
    STATISTIC_REGISTRATION(34),
    STATISTIC_TASK_COMULATIVE(35),
    STATISTIC_CATEGORY_COUNT(36),
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