package aor.paj.utils;

public enum State {
    TODO(100), DOING(200), DONE(300);

    private final int value;
    State(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
