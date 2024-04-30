package finalforeach.cosmicreach.rendering;

public enum RenderOrder {
    DEFAULT(1000),
    TRANSPARENT(2000);

    int order;

    private RenderOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }
}