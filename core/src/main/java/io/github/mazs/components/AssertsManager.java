package io.github.mazs.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class AssertsManager {

    private final Map<String, Texture> cache = new HashMap<>();
    private Texture whitePixel;

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

    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();

        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }
}
