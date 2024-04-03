package aor.paj.entity;//package aor.paj.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity

@Table (name = "message")
public class MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name = "message", nullable = false, unique = false, updatable = true)
    private String message;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false, unique = false, updatable = true)
    private UserEntity sender_id;
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false, unique = false, updatable = true)
    private UserEntity receiver_id;

    @Column(name = "timestamp", nullable = false, unique = false, updatable = true)
    private String timestamp;

    @Column(name = "`read`", nullable = false, unique = false, updatable = true)
    private boolean read;

    public MessageEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setIsRead(boolean isRead) {
        this.read = isRead;
    }
}
