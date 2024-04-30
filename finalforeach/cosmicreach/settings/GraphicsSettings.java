package finalforeach.cosmicreach.settings;

public class GraphicsSettings {
    public static final BooleanSetting greedyMeshingEnabled = new BooleanSetting("greedyMeshingEnabled", false);
    public static final FloatSetting fieldOfView = new FloatSetting("fieldOfView", 70);
    public static final IntSetting renderDistanceInChunks = new IntSetting("renderDistanceInChunks", 16);
    public static final BooleanSetting vSyncEnabled = new BooleanSetting("vSyncEnabled", true);
    public static final IntSetting maxFPS = new IntSetting("maxFPS", 120);
}