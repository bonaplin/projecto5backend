package aor.paj.entity;//package aor.paj.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

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

    @Column(name = "time", nullable = false, unique = false, updatable = true)
    private Instant time;

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

    @PrePersist
    protected void onCreate(){
        time = Instant.now();
        read = false;
    }

    public Instant getTime() {
        return time;
    }

//    public void setTime(Instant time) {
//        this.time = time;
//    }

    public boolean isRead() {
        return read;
    }

    public void setIsRead(boolean read) {
        this.read = read;
    }

    public UserEntity getSender_id() {
        return sender_id;
    }

    public void setSender_id(UserEntity sender_id) {
        this.sender_id = sender_id;
    }

    public UserEntity getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(UserEntity receiver_id) {
        this.receiver_id = receiver_id;
    }
}
