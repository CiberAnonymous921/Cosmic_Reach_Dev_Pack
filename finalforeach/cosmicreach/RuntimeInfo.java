package finalforeach.cosmicreach;

import com.badlogic.gdx.Gdx;
import java.text.DecimalFormat;

public class RuntimeInfo {
    private static final String osName = System.getProperty("os.name").toLowerCase();
    public static final boolean isMac = osName.contains("mac");
    public static final boolean isWindows = osName.contains("windows");
    public static final String version = Gdx.files.internal("build_assets/version.txt").readString();
    public static final boolean useSharedIndices = !isMac;

    public static String getJavaHeapUseStr() {
        return RuntimeInfo.byteSizeToHumanReadable(Gdx.app.getJavaHeap());
    }

    public static String getNativeHeapUseStr() {
        return RuntimeInfo.byteSizeToHumanReadable(Gdx.app.getNativeHeap());
    }

    public static String byteSizeToHumanReadable(long numBytes) {
        long kb = numBytes / 1024L;
        if (kb < 1024L) {
            return kb + " KB";
        }
        long mb = kb / 1024L;
        if (mb < 1024L) {
            return mb + " MB";
        }
        double gb = (float)mb / 1024.0f;
        return new DecimalFormat("#.###").format(gb) + " GB";
    }
}