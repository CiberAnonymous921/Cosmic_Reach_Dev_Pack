package finalforeach.cosmicreach.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import finalforeach.cosmicreach.BlockGame;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.io.SaveLocation;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CrashScreen {
    private static String byteSizeToHumanReadable(long numBytes) {
        return RuntimeInfo.byteSizeToHumanReadable(numBytes);
    }

    /*
     * WARNING - void declaration
     */
    
    @SuppressWarnings("unused")
    public static void showCrash(long startTime, StringBuilder preStartErr, Exception ex) {
        long crashTime = System.currentTimeMillis();
        long ranFor = crashTime - startTime;
        long ranForHours = TimeUnit.MILLISECONDS.toHours(ranFor);
        long ranForMins = TimeUnit.MILLISECONDS.toMinutes(ranFor) - TimeUnit.MILLISECONDS.toHours(ranFor) * 60L;
        long ranForSec = TimeUnit.MILLISECONDS.toSeconds(ranFor) - TimeUnit.MILLISECONDS.toMinutes(ranFor) * 60L;
        long ranForMillis = ranFor - TimeUnit.MILLISECONDS.toSeconds(ranFor) * 1000L;
        String ranForTime = ranFor + " ms";
        if (ranForHours > 0L) {
            ranForTime = ranForHours + " hours, " + ranForMins + " minutes, " + ranForSec + " seconds";
        } else if (ranForMins > 0L) {
            ranForTime = ranForMins + " minutes, " + ranForSec + " seconds";
        } else if (ranForSec > 0L) {
            ranForTime = ranForSec + " seconds, " + ranForMillis + " ms";
        }
        String title = BlockGame.gameStarted ? "Crash while playing Cosmic Reach" : "Could not start Cosmic Reach";
        String infoText = "If writing a bug report, please copy the following logs (don't just screenshot!):";
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        OrderedMap<String, Object> infoItems = new OrderedMap<String, Object>();
        infoItems.put("Game started", BlockGame.gameStarted);
        String gameVersion = "unknown";
        try {
            if (Gdx.files == null) {
                Gdx.files = new Lwjgl3Files();
            }
            gameVersion = Gdx.files.internal("build_assets/version.txt").readString();
        } catch (Exception versionFetchException) {
            preStartErr.append(versionFetchException.toString());
        }
        infoItems.put("Game version", gameVersion);
        infoItems.put("Ran for ", ranForTime);
        infoItems.put("Current time", ZonedDateTime.now().toString().replace("T", " at "));
        infoItems.put("Operating system", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        infoItems.put("Arch", System.getProperty("os.arch"));
        infoItems.put("Java VM name", System.getProperty("java.vm.name"));
        infoItems.put("Java runtime version", System.getProperty("java.runtime.version"));
        infoItems.put("System user language", System.getProperty("user.language"));
        infoItems.put("CPU model", "unknown");
        String osName = System.getProperty("os.name").toLowerCase();
        try {
            int var26_25 = 0;
            List<ProcessBuilder> builders = osName.contains("windows") ? Arrays.asList(new ProcessBuilder("wmic", "cpu", "get", "name")) : (osName.contains("mac") ? Arrays.asList(new ProcessBuilder("sysctl", "-a", "machdep.cpu.brand_string")) : Arrays.asList(new ProcessBuilder("lscpu"), new ProcessBuilder("grep", "Model name"), new ProcessBuilder("cut", "-f", "2", "-d", ":"), new ProcessBuilder("awk", "{$1=$1}1")));
            List<?> processes = ProcessBuilder.startPipeline(builders);
            Process last = (Process)processes.get(processes.size() - 1);
            String string = new String(last.getInputStream().readAllBytes()).replace("\n", "");
            if (osName.contains("windows")) {
                String string2 = string.replace("Name", "").trim();
            }
            infoItems.put("CPU model", var26_25);
        } catch (Exception commandException) {
            commandException.printStackTrace();
        }
        File saveFolder = SaveLocation.getSaveFolder();
        if (saveFolder != null) {
            infoItems.put("Save location free space", CrashScreen.byteSizeToHumanReadable(saveFolder.getFreeSpace()));
            infoItems.put("Save location total space", CrashScreen.byteSizeToHumanReadable(saveFolder.getTotalSpace()));
        }
        infoItems.put("Available processors", Runtime.getRuntime().availableProcessors());
        if (Gdx.app != null) {
            infoItems.put("Native heap use", CrashScreen.byteSizeToHumanReadable(Gdx.app.getNativeHeap()));
            infoItems.put("Java heap use", CrashScreen.byteSizeToHumanReadable(Gdx.app.getJavaHeap()));
        }
        infoItems.put("Max memory available", CrashScreen.byteSizeToHumanReadable(Runtime.getRuntime().maxMemory()));
        infoItems.put("RAM available", "Unknown");
        try {
            String line;
            ProcessBuilder builder = osName.contains("windows") ? new ProcessBuilder("wmic", "ComputerSystem", "get", "TotalPhysicalMemory", "/VALUE") : (osName.contains("mac") ? new ProcessBuilder("sysctl", "-n", "hw.memsize") : new ProcessBuilder("grep", "-i", "memtotal", "/proc/meminfo"));
            Process process = builder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line);
            }
            bufferedReader.close();
            String ramStr = output.toString();
            if (osName.contains("windows")) {
                ramStr = ramStr.replace("TotalPhysicalMemory=", "");
            } else if (!osName.contains("mac")) {
                ramStr = ramStr.replace("MemTotal:", "");
            }
            try {
                String n = ramStr.toLowerCase().trim();
                double num = 0;
                if (n.contains("kb")) {
                    num = Double.parseDouble(n.replace("kb", "").trim()) * 1024.0;
                    ramStr = CrashScreen.byteSizeToHumanReadable((long)num);
                } else {
                    num = Double.parseDouble(n);
                    ramStr = CrashScreen.byteSizeToHumanReadable((long)num);
                }
            } catch (Exception exception) {
                // empty catch block
            }
            infoItems.put("RAM available", ramStr.trim());
        } catch (Exception builder) {
            // empty catch block
        }
        if (Gdx.graphics != null) {
            infoItems.put("getGLVersion", Gdx.graphics.getGLVersion().getDebugVersionString());
        }
        infoItems.put("Prestart error logs", preStartErr);
        infoItems.put("Exception logs", sw);
        Object logText = "";
        for (@SuppressWarnings("rawtypes") ObjectMap.Entry entry : infoItems.entries()) {
            String str;
            if (entry.value == null || (str = entry.value.toString()).isEmpty()) continue;
            boolean addLineBreak = str.contains("\n");
            logText = (String)logText + "* " + (String)entry.key + ": " + (addLineBreak ? "\n" : "") + str + "\n";
        }
        logText = ((String)logText).replace("\t", "    ");
        try {
            new File(SaveLocation.getSaveFolderLocation()).mkdirs();
            File errorLogFile = new File(SaveLocation.getSaveFolderLocation() + "/errorLogLatest.txt");
            try (FileOutputStream fileOutputStream = new FileOutputStream(errorLogFile);){
                fileOutputStream.write(((String)logText).getBytes());
            }
        } catch (Exception fex) {
            fex.printStackTrace();
        }
        JPanel panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, 1);
        panel.setLayout(boxLayout);
        JLabel label = new JLabel(infoText);
        label.setAlignmentX(0.5f);
        panel.add(label);
        JTextArea logTextArea = new JTextArea((String)logText);
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setPreferredSize(new Dimension(1024, 576));
        logScrollPane.setVerticalScrollBarPolicy(22);
        panel.add(logScrollPane);
        JOptionPane.showMessageDialog(null, panel, title, 0);
        ex.printStackTrace();
        System.exit(1);
    }
}