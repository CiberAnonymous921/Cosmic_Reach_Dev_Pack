package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

class FontTexture {
    public int unicodeStart;
    public Texture fontTexture;
    private TextureRegion[] fontTextureRegions;
    private Vector2[] fontCharStartPos;
    private Vector2[] fontCharSizes;

    public FontTexture(int unicodeStart, String fileName) {
        this.unicodeStart = unicodeStart;
        Texture fontTexture = new Texture(fileName);
        int numCols = 16;
        int numRows = 16;
        TextureRegion[][] tr = TextureRegion.split(fontTexture, fontTexture.getWidth() / numCols, fontTexture.getHeight() / numRows);
        this.fontTextureRegions = new TextureRegion[tr.length * tr[0].length];
        this.fontCharStartPos = new Vector2[this.fontTextureRegions.length];
        this.fontCharSizes = new Vector2[this.fontTextureRegions.length];
        TextureData texData = fontTexture.getTextureData();
        texData.prepare();
        Pixmap pix = texData.consumePixmap();
        for (int i = 0; i < numCols; ++i) {
            for (int j = 0; j < numRows; ++j) {
                TextureRegion texReg = tr[j][i];
                int idx = j * numRows + i;
                this.fontTextureRegions[idx] = texReg;
                int maxBoundsX = texReg.getRegionX();
                int maxBoundsY = texReg.getRegionY();
                int minBoundsX = texReg.getRegionX() + texReg.getRegionWidth();
                int minBoundsY = texReg.getRegionY() + texReg.getRegionHeight();
                boolean isFullyTransparent = true;
                for (int px = texReg.getRegionX(); px < texReg.getRegionX() + texReg.getRegionWidth(); ++px) {
                    for (int py = texReg.getRegionY(); py < texReg.getRegionY() + texReg.getRegionHeight(); ++py) {
                        boolean isFullyTransparentPixel;
                        int pixColor = pix.getPixel(px, py);
                        isFullyTransparentPixel = (pixColor & 0xFF) == 0;
                        if (isFullyTransparentPixel) continue;
                        minBoundsX = Math.min(minBoundsX, px);
                        minBoundsY = Math.min(minBoundsY, py);
                        maxBoundsX = Math.max(maxBoundsX, px);
                        maxBoundsY = Math.max(maxBoundsY, py);
                        isFullyTransparent = false;
                    }
                }
                this.fontCharStartPos[idx] = new Vector2(minBoundsX, minBoundsY);
                this.fontCharSizes[idx] = new Vector2(Math.max(0, maxBoundsX - minBoundsX + 1), Math.max(0, maxBoundsY - minBoundsY + 1));
                if (isFullyTransparent) {
                    this.fontCharStartPos[idx].set(texReg.getRegionX(), texReg.getRegionY());
                    this.fontCharSizes[idx].set(texReg.getRegionWidth(), texReg.getRegionHeight());
                }
                texReg.flip(false, true);
            }
        }
        pix.dispose();
    }

    public TextureRegion getTexRegForChar(char c) {
        try {
            return this.fontTextureRegions[c - this.unicodeStart];
        } catch (Exception ex) {
            System.err.println("C: " + c + " | (int):" + c + " | diff: " + (c - this.unicodeStart));
            throw ex;
        }
    }

    public Vector2 getCharStartPos(char c) {
        return this.fontCharStartPos[c - this.unicodeStart];
    }

    public Vector2 getCharSize(char c) {
        return this.fontCharSizes[c - this.unicodeStart];
    }
}