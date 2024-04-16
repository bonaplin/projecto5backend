package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.sql.Timestamp;
import java.time.Instant;

@XmlRootElement
public class MessageDto {
    private String message;
    private String sender;
    private String receiver;
    private Instant time;
    private boolean read;

    public MessageDto() {
    }

    public MessageDto(String message, String sender, String receiver, Instant time, boolean read) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.read = read;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @XmlElement
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @XmlElement
    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @XmlElement
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", time=" + time +
                ", read=" + read +
                '}';
    }
}
