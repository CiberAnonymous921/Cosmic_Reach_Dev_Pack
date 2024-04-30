package finalforeach.cosmicreach.io;

import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.world.World;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public final class SaveLocation {
    public static void OpenFolderWithFileManager(File folder) throws IOException {
        if (!folder.isDirectory()) {
            throw new RuntimeException("`" + folder + "` is not a directory! Does exist: " + folder.exists());
        }
        try {
            if (RuntimeInfo.isMac) {
                Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                throw new RuntimeException("java.awt.Desktop is not supported");
            }
            Desktop.getDesktop().open(folder);
        } catch (Throwable ex) {
            if (RuntimeInfo.isWindows) {
                Runtime.getRuntime().exec("explorer.exe \"" + folder.getAbsolutePath() + "\"");
                return;
            }
            throw ex;
        }
    }

    public static File getSaveFolder() {
        File dir = new File(SaveLocation.getSaveFolderLocation());
        dir.mkdirs();
        return dir;
    }

    public static String getWorldSaveFolderLocation(World world) {
        return SaveLocation.getWorldSaveFolderLocation(world.getWorldFolderName());
    }

    public static String getWorldSaveFolderLocation(String worldFolderName) {
        return SaveLocation.getAllWorldsSaveFolderLocation() + "/" + worldFolderName;
    }

    public static String getAllWorldsSaveFolderLocation() {
        String rootFolderName = SaveLocation.getSaveFolderLocation();
        return rootFolderName + "/worlds";
    }

    public static String getSaveFolderLocation() {
        Object rootFolder;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            rootFolder = System.getenv("LOCALAPPDATA");
        } else if (osName.contains("mac")) {
            rootFolder = System.getenv("HOME") + "/Library";
        } else {
            rootFolder = System.getenv("XDG_DATA_HOME");
            if (rootFolder == null || ((String)rootFolder).isEmpty()) {
                rootFolder = System.getenv("HOME") + "/.local/share";
            }
        }
        String saveFolder = (String)rootFolder + "/cosmic-reach-update";
        return saveFolder;
    }

    public static String getScreenshotFolderLocation() {
        return SaveLocation.getSaveFolderLocation() + "/screenshots";
    }
}