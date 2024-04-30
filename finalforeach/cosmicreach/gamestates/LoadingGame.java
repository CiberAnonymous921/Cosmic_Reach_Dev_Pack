package finalforeach.cosmicreach.gamestates;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.Keybind;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.WorldLoader;
import finalforeach.cosmicreach.world.Zone;

public class LoadingGame extends GameState {
    private DecimalFormat percentFormatter = new DecimalFormat("#%");
    private float animTimer = 0.0f;
    private float tipTimer = 0.0f;
    private int tipIndex;
    private String[] tips;
    private boolean isOdd;

    @Override
    public void create() {
        super.create();
        String[] normalTips = new String[22];
        Pattern keybindPattern = Pattern.compile("\\$keybind_(?<name>[\\w]+)");
        for (int i = 1; i <= normalTips.length; ++i) {
            String tip = Lang.get("Tip" + i);
            if (tip.contains("$keybind")) {
                Matcher m = keybindPattern.matcher(tip);
                tip = m.replaceAll(t -> Keybind.allKeybinds.getOrDefault(t.group().replace("$keybind_", ""), Keybind.MISSINGKEYBIND).getKeyName());
            }
            normalTips[i - 1] = tip;
        }
        String[] oddTips = new String[11];
        for (int i = 1; i <= oddTips.length; ++i) {
            oddTips[i - 1] = Lang.get("OddTip" + i);
        }
        float oddChance = 0.001f;
        if (MathUtils.randomBoolean(oddChance)) {
            this.tips = oddTips;
            this.isOdd = true;
        } else {
            this.tips = normalTips;
        }
        this.tipIndex = MathUtils.random(0, this.tips.length - 1);
        IN_GAME.create();
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (WorldLoader.worldLoader.readyToPlay) {
            Zone defaultZone = InGame.world.getDefaultZone();
            if (defaultZone.spawnPoint == null) {
                defaultZone.calculateSpawn();
                InGame.getLocalPlayer().respawn(defaultZone);
                ChunkSaver.saveWorld(InGame.world);
            }
            System.gc();
            LoadingGame.switchToGameState(IN_GAME);
            return;
        }
        if (this.isOdd) {
            ScreenUtils.clear(0.45f, 0.0078f, 0.0153f, 1.0f, true);
        } else {
            ScreenUtils.clear(0.145f, 0.078f, 0.153f, 1.0f, true);
        }
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
        this.animTimer += Gdx.graphics.getDeltaTime();
        this.tipTimer += Gdx.graphics.getDeltaTime();
        batch.setProjectionMatrix(this.uiCamera.combined);
        batch.begin();
        Object loadingText = Lang.get("Loading");
        float x = MathUtils.cos(this.animTimer) * 10.0f;
        float y = MathUtils.sin(this.animTimer) * 10.0f;
        switch ((int)(this.animTimer * 3.0f) % 4) {
            case 1: {
                loadingText = (String)loadingText + ".";
                break;
            }
            case 2: {
                loadingText = (String)loadingText + "..";
                break;
            }
            case 3: {
                loadingText = (String)loadingText + "...";
            }
        }
        if (this.tipTimer > 5.0f) {
            this.tipIndex = MathUtils.random(0, this.tips.length - 1);
            this.tipTimer = 0.0f;
        }
        String tip = this.tips[this.tipIndex];
        float loadProgress = WorldLoader.worldLoader.loadProgress;
        FontRenderer.drawText(batch, this.uiViewport, (String)loadingText, x, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
        FontRenderer.drawText(batch, this.uiViewport, this.percentFormatter.format(loadProgress), x, y + 16.0f, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
        FontRenderer.drawText(batch, this.uiViewport, tip, 9.0f, -7.0f, HorizontalAnchor.CENTERED, VerticalAnchor.BOTTOM_ALIGNED);
        this.mouse.set(Gdx.input.getX(), Gdx.input.getY());
        this.uiViewport.unproject(this.mouse);
        int numLayers = 10;
        for (int layer = 0; layer < numLayers; ++layer) {
            int num = 320 / numLayers;
            float pTimer = (this.isOdd ? -0.3f : 1.0f) * (this.animTimer + 15.0f) * 0.015f * (float)(layer + 1);
            float maxDist = Vector2.len(this.uiViewport.getWorldWidth() / 2.0f, this.uiViewport.getWorldHeight() / 2.0f) + 100.0f;
            for (int i = 0; i < num; ++i) {
                float pY;
                float mouseDist;
                float a = 30.0f * pTimer + (float)(i * 360 / num);
                float q = i + layer * num;
                float r = (q * q * 7.0f / (float)num + pTimer) * maxDist;
                float minR = 0.0f;
                float pX = x + (minR + (r %= maxDist)) * MathUtils.cosDeg(a + this.animTimer * 2.0f);
                if ((mouseDist = Vector2.dst(pX, pY = y + (minR + r) * MathUtils.sinDeg(a + this.animTimer * 2.0f), this.mouse.x, this.mouse.y)) != 0.0f) {
                    pX += (pX - this.mouse.x) * 30.0f / mouseDist;
                    pY += (pY - this.mouse.y) * 30.0f / mouseDist;
                }
                String pText = ".";
                float g = 1.0f;
                batch.setColor(g, g, g, r / maxDist);
                FontRenderer.drawText(batch, this.uiViewport, pText, pX + 8.0f + 1.0f, pY - 8.0f + 1.0f, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }
}