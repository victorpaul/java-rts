package io.github.mazs.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.HashMap;
import java.util.Map;

public class AssertsManager {

    private final Map<String, Texture> cache = new HashMap<>();
    private Texture whitePixel;
    private BitmapFont defaultFont;

    public Texture getTexture(String path) {
        return cache.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    public Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    public BitmapFont getDefaultFont() {
        if (defaultFont == null) {
            defaultFont = new BitmapFont();
            defaultFont.getData().setScale(0.5f);
        }
        return defaultFont;
    }

    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();

        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }

        if (defaultFont != null) {
            defaultFont.dispose();
            defaultFont = null;
        }
    }
}
