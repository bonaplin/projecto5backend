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
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
