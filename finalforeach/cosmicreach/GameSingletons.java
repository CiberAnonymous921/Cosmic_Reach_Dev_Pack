package finalforeach.cosmicreach;

import finalforeach.cosmicreach.rendering.IWorldRenderingMeshGenThread;
import finalforeach.cosmicreach.rendering.IZoneRenderer;
import finalforeach.cosmicreach.rendering.blockmodels.IBlockModelInstantiator;

public class GameSingletons {
    public static IZoneRenderer zoneRenderer;
    public static IWorldRenderingMeshGenThread meshGenThread;
    public static ISoundManager soundManager;
    public static IBlockModelInstantiator blockModelInstantiator;
}