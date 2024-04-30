package finalforeach.cosmicreach.worldgen.noise;

import java.util.Random;

public class WhiteNoise {
    private long seed;
    private Random rand;

    public WhiteNoise(long seed) {
        this.seed = seed;
        this.rand = new Random(seed);
    }

    public float noise1D(float x) {
        this.rand.setSeed(this.seed + (long)(31 * Float.floatToIntBits(x)));
        return this.rand.nextFloat(-1.0f, 1.0f);
    }

    public float noise2D(float x, float y) {
        float n = this.noise1D(x);
        this.rand.setSeed(this.seed + (long)(31 * Float.floatToIntBits(n)) + (long)Float.floatToIntBits(y));
        return this.rand.nextFloat(-1.0f, 1.0f);
    }

    public float noise3D(float x, float y, float z) {
        float n = this.noise2D(x, y);
        this.rand.setSeed(this.seed + (long)(31 * Float.floatToIntBits(n)) + (long)Float.floatToIntBits(z));
        return this.rand.nextFloat(-1.0f, 1.0f);
    }
}