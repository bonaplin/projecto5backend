package aor.paj.dto;

public class UserStatusUpdateDto {
    private String username;
    private boolean active;

    public UserStatusUpdateDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
