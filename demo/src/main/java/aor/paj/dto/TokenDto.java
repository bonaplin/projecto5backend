package aor.paj.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class TokenDto {
    private String token;
    private Instant expiration;
    private UserDto user;

    public TokenDto() {
    }

    public TokenDto(String token, Instant expiration, UserDto user) {
        this.token = token;
        this.expiration = expiration;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}
