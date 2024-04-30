package finalforeach.cosmicreach.gamestates;

import java.io.File;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UITextInput;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;

public class WorldCreationMenu extends GameState {
    private static String worldTypeStr = Lang.get("worldType");
    private static String worldNameLabel = Lang.get("worldNameButton");
    private static String worldSeedLabel = Lang.get("worldSeedLabel");
    private static String worldSeedLabel2 = Lang.get("worldSeedLabel2");
    String selectedZoneId;
    String worldName = this.getDefaultWorldName();
    String worldSeed;

    private String getDefaultWorldName() {
        return this.getFreeWorldName(Lang.get("defaultWorldName"));
    }

    public String getFileSafeName(String desiredFileName) {
        return desiredFileName.replaceAll("[^a-zA-Z0-9.-]", "_").substring(0, Math.min(255, desiredFileName.length()));
    }

    public String getFreeWorldName(String desiredWorldName) {
        String worldRootLocation = SaveLocation.getAllWorldsSaveFolderLocation();
        if (!new File(worldRootLocation + "/" + this.getFileSafeName(desiredWorldName) + "/worldInfo.json").exists()) {
            return desiredWorldName;
        }
        int i = 1;
        String newWorldName = desiredWorldName + "-" + i;
        while (new File(worldRootLocation + "/" + this.getFileSafeName(newWorldName) + "/worldInfo.json").exists()) {
            newWorldName = desiredWorldName + "-" + ++i;
        }
        return newWorldName;
    }

    public WorldCreationMenu(final GameState previousState) {
        this.previousState = previousState;
        UITextInput worldNameButton = new UITextInput(0.0f, 25.0f, 300.0f, 50.0f){

            @Override
            public String getDefaultInputText() {
                return WorldCreationMenu.this.getDefaultWorldName();
            }

            @Override
            public void updateText() {
                super.updateText();
                WorldCreationMenu.this.worldName = this.inputText;
            }
        };
        worldNameButton.labelPrefix = worldNameLabel;
        worldNameButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
        worldNameButton.hAnchor = HorizontalAnchor.CENTERED;
        worldNameButton.show();
        this.uiObjects.add(worldNameButton);
        UITextInput worldSeedButton = new UITextInput(0.0f, 100.0f, 300.0f, 50.0f){

            @Override
            public void updateText() {
                super.updateText();
                WorldCreationMenu.this.worldSeed = this.inputText;
                this.labelPrefix = "".equals(this.inputText) ? worldSeedLabel : worldSeedLabel2;
            }
        };
        worldSeedButton.labelPrefix = worldSeedLabel2;
        worldSeedButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
        worldSeedButton.hAnchor = HorizontalAnchor.CENTERED;
        worldSeedButton.show();
        this.uiObjects.add(worldSeedButton);
        UIElement worldTypeButton = new UIElement(0.0f, 175.0f, 300.0f, 50.0f){
            String[] generatorKeys;
            int selectedIdx;
            {
                this.selectedIdx = 0;
            }

            @Override
            public void onCreate() {
                super.onCreate();
                Set<String> ids = ZoneGenerator.getZoneGeneratorSaveKeys();
                this.generatorKeys = ids.toArray(new String[ids.size()]);
                this.updateChoice();
            }

            @Override
            public void onClick() {
                super.onClick();
                ++this.selectedIdx;
                this.selectedIdx %= this.generatorKeys.length;
                this.updateChoice();
            }

            private void updateChoice() {
                WorldCreationMenu.this.selectedZoneId = this.generatorKeys[this.selectedIdx];
                this.setText(worldTypeStr + ZoneGenerator.getZoneGeneratorName(WorldCreationMenu.this.selectedZoneId));
            }
        };
        worldTypeButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
        worldTypeButton.hAnchor = HorizontalAnchor.CENTERED;
        worldTypeButton.show();
        this.uiObjects.add(worldTypeButton);
        UIElement createNewWorldButton = new UIElement(-275.0f, -16.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(GameState.LOADING_GAME);
                World world = World.createNew(WorldCreationMenu.this.getFreeWorldName(WorldCreationMenu.this.worldName), WorldCreationMenu.this.worldSeed, WorldCreationMenu.this.selectedZoneId, ZoneGenerator.getZoneGenerator(WorldCreationMenu.this.selectedZoneId));
                GameState.IN_GAME.loadWorld(world);
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
                GameState.switchToGameState(previousState);
            }
        };
        returnButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        returnButton.setText(Lang.get("returnButton"));
        returnButton.show();
        this.uiObjects.add(returnButton);
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (Gdx.input.isKeyJustPressed(111)) {
            if (UIElement.activeElement != null) {
                UIElement.activeElement.deactivate();
                UIElement.activeElement = null;
            } else {
                WorldCreationMenu.switchToGameState(this.previousState);
            }
        }
        ScreenUtils.clear(0.145f, 0.078f, 0.153f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
    }
}