package aor.paj.dto;

public class TokenAndRoleDto {
    private String token;
    private String role;
    private String username;
    private boolean confirmed;

    public TokenAndRoleDto() {
    }

    public TokenAndRoleDto(String token, String role, String username, boolean confirmed){
        this.token = token;
        this.role = role;
        this.username = username;
        this.confirmed = confirmed;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
