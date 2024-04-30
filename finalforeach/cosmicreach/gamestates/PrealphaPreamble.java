package finalforeach.cosmicreach.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class PrealphaPreamble extends GameState {
    private String prealphaWarningText = Lang.get("preAlphaText");
    private String[] prealphaWarningTextLines = this.prealphaWarningText.split("\n");
    float animTime;

    @Override
    public void create() {
        super.create();
        UIElement okButton = new UIElement(0.0f, -50.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new MainMenu());
            }
        };
        okButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        okButton.setText(Lang.get("okButton"));
        okButton.show();
        this.uiObjects.add(okButton);
    }

    @Override
    public void render(float partTick) {
        if (Lang.currentLang == null) {
            GameState.switchToGameState(new LanguagesMenu(this));
            return;
        }
        super.render(partTick);
        this.animTime += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.0f, 0.0f, 0.0f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
        Texture textLogo = MainMenu.textLogo;
        batch.setProjectionMatrix(this.uiCamera.combined);
        batch.begin();
        float scale = 4.0f;
        float logoW = 192.0f;
        float logoH = 64.0f;
        float logoX = -scale * logoW / 2.0f;
        float logoY = -this.uiViewport.getWorldHeight() / 2.0f;
        batch.draw(textLogo, logoX, logoY, 0.0f, 0.0f, logoW, logoH, scale, scale, 0.0f, 0, 0, textLogo.getWidth(), textLogo.getHeight(), false, true);
        float y = -50.0f;
        Vector2 lineDim = new Vector2();
        for (int i = 0; i < this.prealphaWarningTextLines.length; ++i) {
            float maxW = this.uiViewport.getWorldWidth() - 128.0f;
            String line = this.prealphaWarningTextLines[i].trim();
            FontRenderer.getTextDimensions(this.uiViewport, line, lineDim);
            if (lineDim.x > maxW) {
                String[] words = line.split(" ");
                Object curLine = "";
                for (int w = 0; w < words.length; ++w) {
                    String word = words[w];
                    float cw = ((String)curLine).length() == 0 ? 0.0f : lineDim.x;
                    FontRenderer.getTextDimensions(this.uiViewport, word, lineDim);
                    lineDim.x += cw;
                    if (lineDim.x > maxW) {
                        if (((String)curLine).length() == 0) {
                            FontRenderer.drawText(batch, this.uiViewport, word, -8.0f, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
                        } else {
                            FontRenderer.drawText(batch, this.uiViewport, (String)curLine, -8.0f, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
                        }
                        y += 32.0f;
                        lineDim.set(0.0f, 0.0f);
                        if (((String)curLine).length() == 0) continue;
                        curLine = word;
                        continue;
                    }
                    curLine = (String)curLine + word;
                    if (w >= words.length - 1) continue;
                    curLine = (String)curLine + " ";
                }
                if (((String)curLine).length() <= 0) continue;
                FontRenderer.drawText(batch, this.uiViewport, (String)curLine, -8.0f, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
                y += 32.0f;
                lineDim.set(0.0f, 0.0f);
                curLine = "";
                continue;
            }
            FontRenderer.drawText(batch, this.uiViewport, line, -8.0f, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
            y += 32.0f;
        }
        batch.end();
    }
}