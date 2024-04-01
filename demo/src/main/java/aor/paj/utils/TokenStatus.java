package aor.paj.utils;

public enum TokenStatus {
    VALID("Token is valid"),
    EXPIRED("Token expired, please login again"),
    NOT_FOUND("Token not found, please login"),
    NOT_CONFIRMED("User not confirmed, please confirm your email");

    private final String message;

    TokenStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}