package finalforeach.cosmicreach.gamestates;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.World;

public class WorldSelectionMenu extends GameState {
    Array<UIElement> worldButtons = new Array<UIElement>();
    int topWorldIdx;
    UIElement upButton;
    UIElement downButton;

    private void cycleWorldButtons() {
        if (this.upButton != null) {
            if (this.topWorldIdx > 0) {
                this.upButton.show();
            } else {
                this.upButton.hide();
            }
        }
        if (this.downButton != null) {
            this.downButton.hide();
        }
        float y = 0.0f;
        for (int i = 0; i < this.worldButtons.size; ++i) {
            UIElement worldButton = this.worldButtons.get(i);
            if (i < this.topWorldIdx) {
                worldButton.hide();
                continue;
            }
            worldButton.show();
            worldButton.y = y + 16.0f;
            y += 75.0f;
            if (!(y > 550.0f)) continue;
            worldButton.hide();
            if (this.downButton == null) continue;
            this.downButton.show();
        }
    }

    @Override
    public void create() {
        super.create();
        String worldRootLocation = SaveLocation.getAllWorldsSaveFolderLocation();
        final File worldsDir = new File(worldRootLocation);
        final WorldSelectionMenu thisGameState = this;
        worldsDir.mkdirs();
        float x = 0.0f;
        float y = 0.0f;
        Object[] allWorlds = worldsDir.list();
        Arrays.sort(allWorlds);
        Json json = new Json();
        for (Object worldFolderName : allWorlds) {
            World w;
            File worldInfoFile = new File(worldRootLocation + "/" + (String)worldFolderName + "/worldInfo.json");
            if (!worldInfoFile.exists() || (w = json.fromJson(World.class, Gdx.files.absolute(worldInfoFile.getAbsolutePath()).readString())) == null) continue;
            w.worldFolderName = (String) worldFolderName;
            UIElement enterWorldButton = new UIElement(x, y + 16.0f, 250.0f, 50.0f){
                @Override
                public void onClick() {
                    super.onClick();
                    GameState.switchToGameState(GameState.LOADING_GAME);
                    GameState.IN_GAME.loadWorld((String) worldFolderName);
                }
            };
            this.worldButtons.add(enterWorldButton);
            y += 75.0f;
            enterWorldButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
            enterWorldButton.hAnchor = HorizontalAnchor.CENTERED;
            enterWorldButton.setText(w.getDisplayName());
            enterWorldButton.show();
            this.uiObjects.add(enterWorldButton);
        }
        this.upButton = new UIElement(x + 50.0f + 125.0f, -50.0f, 50.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                WorldSelectionMenu.this.topWorldIdx = MathUtils.clamp(WorldSelectionMenu.this.topWorldIdx - 1, 0, WorldSelectionMenu.this.worldButtons.size - 1);
                WorldSelectionMenu.this.cycleWorldButtons();
            }
        };
        this.upButton.vAnchor = VerticalAnchor.CENTERED;
        this.upButton.hAnchor = HorizontalAnchor.CENTERED;
        this.upButton.setText("^");
        this.upButton.show();
        this.uiObjects.add(this.upButton);
        this.downButton = new UIElement(x + 50.0f + 125.0f, 50.0f, 50.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                WorldSelectionMenu.this.topWorldIdx = MathUtils.clamp(WorldSelectionMenu.this.topWorldIdx + 1, 0, WorldSelectionMenu.this.worldButtons.size - 1);
                WorldSelectionMenu.this.cycleWorldButtons();
            }
        };
        this.downButton.vAnchor = VerticalAnchor.CENTERED;
        this.downButton.hAnchor = HorizontalAnchor.CENTERED;
        this.downButton.setText("V");
        this.downButton.show();
        this.uiObjects.add(this.downButton);
        this.cycleWorldButtons();
        UIElement createNewWorldButton = new UIElement(-275.0f, -16.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new WorldCreationMenu(thisGameState));
            }
        };
        createNewWorldButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        createNewWorldButton.setText(Lang.get("Create_New_World"));
        createNewWorldButton.show();
        this.uiObjects.add(createNewWorldButton);
        UIElement returnButton = new UIElement(0.0f, -16.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new MainMenu());
            }
        };
        returnButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        returnButton.setText(Lang.get("Return_to_Main_Menu"));
        returnButton.show();
        this.uiObjects.add(returnButton);
        UIElement loadButton = new UIElement(275.0f, -16.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                try {
                    SaveLocation.OpenFolderWithFileManager(worldsDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        loadButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        loadButton.setText(Lang.get("Open_Worlds_Directory"));
        loadButton.show();
        this.uiObjects.add(loadButton);
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        ScreenUtils.clear(0.0f, 0.0f, 0.0f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
    }
}