package aor.paj.entity;

import jakarta.inject.Named;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="token")
@NamedQuery(name = "Token.findTokenByToken", query = "SELECT t FROM TokenEntity t WHERE t.token = :token")
@NamedQuery(name = "Token.findTokenByUserId", query = "SELECT t FROM TokenEntity t WHERE t.user.id = :id")
@NamedQuery(name = "Token.findAllTokens", query = "SELECT t FROM TokenEntity t")


public class TokenEntity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name="id", nullable = false, unique = true, updatable = false)
        private int id;

        @Column(name="token", nullable = false, unique = true, updatable = true)
        private String token;

        @Column(name="expiration", nullable = false, unique = false, updatable = true)
        private LocalDateTime expiration;

        @ManyToOne
        @JoinColumn(name="user_id", nullable = false)
        private UserEntity user;

        public TokenEntity() {
        }

        public int getId() {
            return id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public LocalDateTime getExpiration() {
            return expiration;
        }

        public void setExpiration(LocalDateTime expiration) {
            this.expiration = expiration;
        }

        public UserEntity getUser() {
            return user;
        }

        public void setUser(UserEntity user) {
            this.user = user;
        }

    @Override
    public String toString() {
        return "TokenEntity{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", expiration=" + expiration +
                ", user=" + user +
                '}';
    }
}
