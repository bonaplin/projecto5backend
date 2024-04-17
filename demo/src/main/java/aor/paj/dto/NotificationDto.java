package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Instant;

@XmlRootElement
public class NotificationDto extends MessageDto{

    public NotificationDto() {
    }

    public NotificationDto(int id, String message, String sender, String receiver, Instant time, boolean read) {
        super(id, message, sender, receiver, time, read);
    }


    @Override
    public String toString() {
        return super.toString();
    }
}

