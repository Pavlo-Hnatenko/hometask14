package entity;

public class Problem {

    private final int fromId;
    private final int toId;

    public Problem(int fromId, int toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }
}
