package finalforeach.cosmicreach.worldgen;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateInstantiator;
import finalforeach.cosmicreach.world.Zone;

public abstract class ZoneGenerator {
    public static BlockStateInstantiator BLOCKSTATE_INSTANTIATOR;
    private static final Object REGISTER_ZONE_LOCK;
    private static HashMap<String, Class<? extends ZoneGenerator>> allZoneGenerators;
    private static HashMap<String, String> zoneIdsAndNames;
    public static int CHUNK_WIDTH;
    public long seed = new Random().nextLong();

    public abstract String getSaveKey();

    public abstract void create();

    public abstract void generateForChunkColumn(Zone var1, ChunkColumn var2);

    public static ZoneGenerator getZoneGenerator(String zoneGenSaveKey) {
        return ZoneGenerator.getZoneGenerator(zoneGenSaveKey, 0L);
    }

    public static ZoneGenerator getZoneGenerator(String zoneGenSaveKey, long zoneSeed) {
        ZoneGenerator.registerZoneGenerators();
        Class<? extends ZoneGenerator> worldGenClass = allZoneGenerators.get(zoneGenSaveKey);
        try {
            Constructor<? extends ZoneGenerator> constructor = worldGenClass.getDeclaredConstructor(new Class[0]);
            constructor.setAccessible(true);
            ZoneGenerator worldGen = constructor.newInstance(new Object[0]);
            worldGen.seed = zoneSeed;
            worldGen.create();
            return worldGen;
        } catch (Exception e) {
            throw new RuntimeException("Could not find zone generator for worldGenSaveKey: " + zoneGenSaveKey, e);
        }
    }

    public static Set<String> getZoneGeneratorSaveKeys() {
        ZoneGenerator.registerZoneGenerators();
        return zoneIdsAndNames.keySet();
    }

    public static String getZoneGeneratorName(String id) {
        return zoneIdsAndNames.get(id);
    }

    protected BlockState getBlockStateInstance(String blockStateId) {
        return BLOCKSTATE_INSTANTIATOR.getBlockStateInstance(blockStateId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerZoneGenerators() {
        if (allZoneGenerators != null) {
            return;
        }
        Object object = REGISTER_ZONE_LOCK;
        synchronized (object) {
            if (allZoneGenerators != null) {
                return;
            }
            allZoneGenerators = new HashMap<String, Class<? extends ZoneGenerator>>();
            ZoneGenerator.registerZoneGenerator(new MoonZoneGenerator());
            ZoneGenerator.registerZoneGenerator(new FlatZoneGenerator());
            ZoneGenerator.registerZoneGenerator(new NostalgicIslandZoneGenerator());
        }
    }

    public static void registerZoneGenerator(ZoneGenerator zoneGenerator) {
        try {
            if (zoneGenerator.getClass().getDeclaredConstructor(new Class[0]) != null) {
                zoneIdsAndNames.put(zoneGenerator.getSaveKey(), zoneGenerator.getName());
                allZoneGenerators.put(zoneGenerator.getSaveKey(), zoneGenerator.getClass());
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(zoneGenerator.getClass().getSimpleName() + " requires a zero-arg constructor.", e);
        }
    }

    protected abstract String getName();

    static {
        REGISTER_ZONE_LOCK = new Object();
        zoneIdsAndNames = new HashMap<String, String>();
        CHUNK_WIDTH = 16;
    }
}