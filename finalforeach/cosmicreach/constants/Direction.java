package finalforeach.cosmicreach.constants;

public enum Direction {
	NEG_X(-1, 0, 0),
    POS_X(1, 0, 0),
    NEG_Y(0, -1, 0),
    POS_Y(0, 1, 0),
    NEG_Z(0, 0, -1),
    POS_Z(0, 0, 1);

    public static final Direction[] ALL_DIRECTIONS;
    public static final Direction[] ALL_POS_AXIS;
    public static final Direction[] VERT_AXIS;
    public static final Direction[] NEG_Y_ARRAY;
    private int xOff;
    private int yOff;
    private int zOff;

    private Direction(int xOff, int yOff, int zOff) {
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
    }

    public int getXOffset() {
        return this.xOff;
    }

    public int getYOffset() {
        return this.yOff;
    }

    public int getZOffset() {
        return this.zOff;
    }

    public boolean isXAxis() {
        return this.xOff != 0;
    }

    public boolean isYAxis() {
        return this.yOff != 0;
    }

    public boolean isZAxis() {
        return this.zOff != 0;
    }

    static {
        ALL_DIRECTIONS = Direction.values();
        ALL_POS_AXIS = new Direction[]{POS_X, POS_Y, POS_Z};
        VERT_AXIS = new Direction[]{NEG_Y, POS_Y};
        NEG_Y_ARRAY = new Direction[]{NEG_Y};
    }
}