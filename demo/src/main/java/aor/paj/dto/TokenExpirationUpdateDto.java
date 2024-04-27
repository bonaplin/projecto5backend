package aor.paj.dto;

public class TokenExpirationUpdateDto {
    private int defaultTokenExpirationMinutes;
    private int poTokenExpirationMinutes;

    public TokenExpirationUpdateDto() {
    }

    public TokenExpirationUpdateDto(int defaultTokenExpirationMinutes, int poTokenExpirationMinutes) {
        this.defaultTokenExpirationMinutes = defaultTokenExpirationMinutes;
        this.poTokenExpirationMinutes = poTokenExpirationMinutes;
    }

    public int getDefaultTokenExpirationMinutes() {
        return defaultTokenExpirationMinutes;
    }

    public int getPoTokenExpirationMinutes() {
        return poTokenExpirationMinutes;
    }

    public void setDefaultTokenExpirationMinutes(int defaultTokenExpirationMinutes) {
        this.defaultTokenExpirationMinutes = defaultTokenExpirationMinutes;
    }

    public void setPoTokenExpirationMinutes(int poTokenExpirationMinutes) {
        this.poTokenExpirationMinutes = poTokenExpirationMinutes;
    }

    @Override
    public String toString() {
        return "TokenExpirationUpdateDto{" +
                "defaultTokenExpirationMinutes=" + defaultTokenExpirationMinutes +
                ", poTokenExpirationMinutes=" + poTokenExpirationMinutes +
                '}';
    }
}
