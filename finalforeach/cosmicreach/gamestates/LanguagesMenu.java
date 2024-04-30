package finalforeach.cosmicreach.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UIObject;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.ui.layouts.UIFlowLayout;
import finalforeach.cosmicreach.ui.layouts.UILayout;

public class LanguagesMenu extends GameState {
    UILayout layout;

    public LanguagesMenu(GameState previousState) {
        this.previousState = previousState;
    }

    @Override
    public void create() {
        super.create();
        float x = 0.0f;
        float y = 0.0f;
        this.layout = new UIFlowLayout(0.0f, 0.0f, this.uiViewport.getWorldWidth(), this.uiViewport.getWorldHeight());
        for (final Lang lang : Lang.getLanguages()) {
            UIElement langButton = new UIElement(x, y + 16.0f, 250.0f, 50.0f){

                @Override
                public void onClick() {
                    super.onClick();
                    if (Lang.currentLang == null) {
                        lang.select();
                        LanguagesMenu.this.returnToPrevious();
                        return;
                    }
                    lang.select();
                    for (int i = 0; i < LanguagesMenu.this.uiObjects.size; ++i) {
                        ((UIObject)LanguagesMenu.this.uiObjects.get(i)).updateText();
                    }
                }

                @Override
                public void updateText() {
                    if (lang.isCurrentLanguage()) {
                        this.setText("[" + lang.getName() + "]");
                    } else {
                        this.setText(lang.getName());
                    }
                }
            };
            y += 60.0f;
            langButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
            langButton.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
            langButton.updateText();
            langButton.show();
            this.layout.add(langButton);
        }
        this.layout.show();
        this.uiObjects.add(this.layout);
        if (Lang.currentLang != null) {
            UIElement doneButton = new UIElement(0.0f, 200.0f, 250.0f, 50.0f){

                @Override
                public void onClick() {
                    super.onClick();
                    LanguagesMenu.this.returnToPrevious();
                }

                @Override
                public void updateText() {
                    super.updateText();
                    this.setText(Lang.get("doneButton"));
                }
            };
            doneButton.updateText();
            doneButton.show();
            this.uiObjects.add(doneButton);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.layout.setWidth(this.uiViewport.getWorldWidth());
        this.layout.setHeight(this.uiViewport.getWorldHeight() - 200.0f);
    }

    private void returnToPrevious() {
        if (this.previousState instanceof PrealphaPreamble) {
            LanguagesMenu.switchToGameState(new PrealphaPreamble());
        } else {
            LanguagesMenu.switchToGameState(new OptionsMenu(this.previousState.previousState));
        }
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (Gdx.input.isKeyJustPressed(111)) {
            this.returnToPrevious();
        }
        ScreenUtils.clear(0.45f, 0.6f, 7.0f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
    }
}