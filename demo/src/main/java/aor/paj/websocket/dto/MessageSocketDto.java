package aor.paj.websocket.dto;

public class MessageSocketDto {
    private String message;
    private String senderToken;
    private String receiverUsername;
    private int type;

    public MessageSocketDto() {
    }

    public MessageSocketDto(String message, String senderToken, String receiverUsername, int type) {
        this.message = message;
        this.senderToken = senderToken;
        this.receiverUsername = receiverUsername;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    @Override
    public String toString() {
        return "MessageSocketDto{" +
                "message='" + message + '\'' +
                ", senderToken='" + senderToken + '\'' +
                ", receiverUsername='" + receiverUsername + '\'' +
                ", type=" + type +
                '}';
    }
}
