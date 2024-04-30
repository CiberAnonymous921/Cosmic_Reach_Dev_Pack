package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class FontRenderer {
    public static FontTexture fontBasic = new FontTexture(0, "lang/textures/cosmic-reach-font-000-basic.png");
    public static FontTexture fontLatin1Sup = new FontTexture(256, "lang/textures/cosmic-reach-font-0100-extended-A.png");
    public static FontTexture fontCyrillic = new FontTexture(1024, "lang/textures/cosmic-reach-font-0400-cyrillic.png");
    private static Vector2 tmpTextDim = new Vector2();

    public static FontTexture getFontTexOfChar(char c) {
        if (c < '\u0100') {
            return fontBasic;
        }
        if (c - 256 < 256 && c >= '\u0100') {
            return fontLatin1Sup;
        }
        if (c - 1024 < 256 && c >= '\u0400') {
            return fontCyrillic;
        }
        return null;
    }

    public static Vector2 getTextDimensions(Viewport uiViewport, String text, Vector2 textDim) {
        float x = 0.0f;
        float y = 0.0f;
        float xOff = 0.0f;
        float yOff = 0.0f;
        block4: for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            FontTexture f = FontRenderer.getFontTexOfChar(c);
            if (f == null) {
                c = '?';
                f = FontRenderer.getFontTexOfChar(c);
            }
            TextureRegion texReg = f.getTexRegForChar(c);
            xOff -= f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth();
            switch (c) {
                case '\n': {
                    y += (float)texReg.getRegionHeight();
                    x = Math.max(x, xOff);
                    xOff = 0.0f;
                    yOff = 0.0f;
                    continue block4;
                }
                case ' ': {
                    xOff += f.getCharSize((char)c).x / 4.0f;
                    continue block4;
                }
                default: {
                    xOff += f.getCharSize((char)c).x + f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth() + 2.0f;
                    yOff = Math.max(yOff, f.getCharSize((char)c).y + f.getCharStartPos((char)c).y % (float)texReg.getRegionHeight());
                }
            }
        }
        textDim.set(Math.max(x, xOff), y + yOff);
        return textDim;
    }

    public static void drawText(SpriteBatch batch, Viewport uiViewport, String text, float xStart, float yStart, HorizontalAnchor hAnchor, VerticalAnchor vAnchor) {
        Vector2 textDim = FontRenderer.getTextDimensions(uiViewport, text, tmpTextDim);
        float w = textDim.x;
        float h = textDim.y;
        switch (hAnchor) {
            case LEFT_ALIGNED: {
                xStart -= uiViewport.getWorldWidth() / 2.0f;
                break;
            }
            case RIGHT_ALIGNED: {
                xStart = xStart + uiViewport.getWorldWidth() / 2.0f - w;
                break;
            }
            default: {
                xStart -= w / 2.0f;
            }
        }
        switch (vAnchor) {
            case TOP_ALIGNED: {
                yStart -= uiViewport.getWorldHeight() / 2.0f;
                break;
            }
            case BOTTOM_ALIGNED: {
                yStart = yStart + uiViewport.getWorldHeight() / 2.0f - h;
                break;
            }
            default: {
                yStart -= h / 2.0f;
            }
        }
        FontRenderer.drawText(batch, uiViewport, text, xStart, yStart);
    }

    public static void drawText(SpriteBatch batch, Viewport uiViewport, String text, float xStart, float yStart) {
        float x = xStart;
        float y = yStart;
        Texture lastBound = null;
        block4: for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            FontTexture f = FontRenderer.getFontTexOfChar(c);
            if (f == null) {
                c = '?';
                f = FontRenderer.getFontTexOfChar(c);
            }
            TextureRegion texReg = f.getTexRegForChar(c);
            if (lastBound != f.fontTexture) {
                f.fontTexture.bind(0);
                lastBound = f.fontTexture;
            }
            x -= f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth();
            switch (c) {
                case '\n': {
                    y += (float)texReg.getRegionHeight();
                    x = xStart;
                    continue block4;
                }
                case ' ': {
                    x += f.getCharSize((char)c).x / 4.0f;
                    continue block4;
                }
                default: {
                    batch.draw(texReg, x, y);
                    x += f.getCharSize((char)c).x + f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth() + 2.0f;
                }
            }
        }
    }

    public static void drawTextbox(SpriteBatch batch, Viewport uiViewport, String text, float xStart, float yStart, float maxWidth) {
        float x = xStart;
        float y = yStart;
        Texture lastBound = null;
        block4: for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            FontTexture f = FontRenderer.getFontTexOfChar(c);
            if (f == null) {
                c = '?';
                f = FontRenderer.getFontTexOfChar(c);
            }
            TextureRegion texReg = f.getTexRegForChar(c);
            if (lastBound != f.fontTexture) {
                f.fontTexture.bind(0);
                lastBound = f.fontTexture;
            }
            float cx = f.getCharStartPos((char)c).x;
            float w = f.getCharSize((char)c).x;
            x -= cx % (float)texReg.getRegionWidth();
            if (c != '\n' && x + w > xStart + maxWidth) {
                y += (float)texReg.getRegionHeight();
                x = xStart;
            }
            switch (c) {
                case '\n': {
                    y += (float)texReg.getRegionHeight();
                    x = xStart;
                    continue block4;
                }
                case ' ': {
                    x += w / 4.0f;
                    continue block4;
                }
                default: {
                    batch.draw(texReg, x, y);
                    x += w + cx % (float)texReg.getRegionWidth() + 2.0f;
                }
            }
        }
    }
}