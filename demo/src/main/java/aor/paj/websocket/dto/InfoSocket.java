package aor.paj.websocket.dto;

public class InfoSocket {
    private String message;
    private String receiver;
    private int type;

    public InfoSocket() {
    }

    public InfoSocket(String message, String receiver, int type) {
        this.message = message;
        this.receiver = receiver;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "InfoSocket{" +
                "message='" + message + '\'' +
                ", receiver='" + receiver + '\'' +
                ", type=" + type +
                '}';
    }
}
