package finalforeach.cosmicreach.lwjgl3;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import finalforeach.cosmicreach.BlockGame;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.PauseMenu;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;

public class Lwjgl3Launcher {
    static long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }
        final StringBuilder preStartErr = new StringBuilder();
        final PrintStream defaultErr = System.err;
        try {
            System.setErr(new PrintStream(new OutputStream(){

                @Override
                public void write(int b) throws IOException {
                    defaultErr.write(b);
                    if (!BlockGame.gameStarted) {
                        preStartErr.append(Character.toChars(b));
                    } else {
                        System.setErr(defaultErr);
                    }
                }
            }));
            Lwjgl3Launcher.createApplication();
        } catch (Exception ex) {
            CrashScreen.showCrash(startTime, preStartErr, ex);
        }
    }

    private static Lwjgl3Application createApplication() {
        Lwjgl3Application a = new Lwjgl3Application(new BlockGame(), Lwjgl3Launcher.getDefaultConfiguration());
        return a;
    }

    private static void setupForMac(Lwjgl3ApplicationConfiguration configuration) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac")) {
            return;
        }
        ShaderProgram.prependVertexCode = GameShader.macOSPrependVertVer;
        ShaderProgram.prependFragmentCode = GameShader.macOSPrependFragVer;
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2);
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        Lwjgl3Launcher.setupForMac(configuration);
        configuration.setTitle("Cosmic Reach");
        configuration.useVsync(GraphicsSettings.vSyncEnabled.getValue());
        configuration.enableGLDebugOutput(true, System.err);
        configuration.setForegroundFPS(0);
        configuration.setWindowedMode(1024, 576);
        configuration.setWindowIcon("textures/logox128.png", "textures/logox64.png", "textures/logox48.png", "textures/logox32.png", "textures/logox16.png");
        configuration.setWindowListener(new Lwjgl3WindowListener(){

            @Override
            public void focusLost() {
                BlockGame.isFocused = false;
                if (GameState.currentGameState == GameState.IN_GAME) {
                    GameState.switchToGameState(new PauseMenu(Gdx.input.isCursorCatched()));
                    Gdx.input.setCursorCatched(false);
                }
            }

            @Override
            public void focusGained() {
                BlockGame.isFocused = true;
            }

            @Override
            public void created(Lwjgl3Window window) {
            }

            @Override
            public void iconified(boolean isIconified) {
            }

            @Override
            public void maximized(boolean isMaximized) {
            }

            @Override
            public boolean closeRequested() {
                return true;
            }

            @Override
            public void filesDropped(String[] files) {
            }

            @Override
            public void refreshRequested() {
            }
        });
        return configuration;
    }
}