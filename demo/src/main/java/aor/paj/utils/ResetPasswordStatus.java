package aor.paj.utils;

public enum ResetPasswordStatus {
    SUCCESS("Password reset successfully"),
    USER_NOT_FOUND("User not found"),
    TOKEN_EXPIRED("Token expired, please request a new one"),
    USER_UNCONFIRMED("User not confirmed, please confirm your email");

    private final String message;

    ResetPasswordStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
