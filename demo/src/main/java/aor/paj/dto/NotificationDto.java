package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Instant;

@XmlRootElement
public class NotificationDto {
    private String receiver;
    private String message;
    private Instant time;
    private boolean read;

    public NotificationDto() {
    }

    public NotificationDto(String receiver, String message, Instant time, boolean read) {
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.read = read;
    }

    @XmlElement
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}
