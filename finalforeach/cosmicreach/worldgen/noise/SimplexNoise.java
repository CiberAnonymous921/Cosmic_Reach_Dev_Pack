package finalforeach.cosmicreach.worldgen.noise;

import libs.opensimplex2.OpenSimplex2;

public class SimplexNoise {
    private long seed;

    public SimplexNoise(long seed) {
        this.seed = seed;
    }

    public float noise2(float x, float y) {
        return OpenSimplex2.noise2(this.seed, x, y);
    }

    public float noise3_XZBeforeY(float x, float y, float z) {
        return OpenSimplex2.noise3_ImproveXZ(this.seed, x, y, z);
    }
}