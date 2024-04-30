package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.graphics.Camera;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.Zone;

public interface IZoneRenderer {
    public void dispose();

    public void render(Zone var1, Camera var2);

    public void removeRegion(Region var1);

    public void unload();
}