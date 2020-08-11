package entity;

public class Route {

    private final int fromId;
    private final int toId;
    private final int cost;

    public Route(int fromId, int toId, int cost) {
        this.fromId = fromId;
        this.toId = toId;
        this.cost = cost;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public int getCost() {
        return cost;
    }
}
