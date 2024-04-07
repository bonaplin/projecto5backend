package aor.paj.entity;//package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity

@Table (name = "notification")
public class NotificationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name="receiver_id", nullable = false)
    private UserEntity receiver;

//    @OneToOne
//    @JoinColumn(name="message")
    @Column(name="message")
    private String message;

    @Column(name="time", nullable = false, unique = false, updatable = true)
    private Instant time;

    @Column(name="`read`", nullable = false, unique = false, updatable = true)
    private boolean read;
    public NotificationEntity() {
    }

    public int getId() {
        return id;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTime() {
        return time;
    }

    @PrePersist
    protected void onCreate(){
        time = Instant.now();
        read = false;
    }
//
//    public void setTime(Instant time) {
//        this.time = time;
//    }

    public boolean isRead() {
        return read;
    }

    public void setIsRead(boolean read) {
        this.read = read;
    }

}
