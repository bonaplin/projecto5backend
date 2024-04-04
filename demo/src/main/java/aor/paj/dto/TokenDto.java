package aor.paj.dto;

import java.time.LocalDateTime;

public class TokenDto {
    private String token;
    private LocalDateTime expiration;
    private UserDto user;

    public TokenDto() {
    }

    public TokenDto(String token, LocalDateTime expiration, UserDto user) {
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

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}
