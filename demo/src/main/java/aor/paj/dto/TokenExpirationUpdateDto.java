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


    @Override
    public String toString() {
        return "TokenExpirationUpdateDto{" +
                "defaultTokenExpirationMinutes=" + defaultTokenExpirationMinutes +
                ", poTokenExpirationMinutes=" + poTokenExpirationMinutes +
                '}';
    }
}
