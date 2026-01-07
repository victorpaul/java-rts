package io.github.mazs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DebugDrawComponent {
    private final AssertsManager assertsManager;

    public DebugDrawComponent(AssertsManager assertsManager) {
        this.assertsManager = assertsManager;
    }

    public void drawPixel(SpriteBatch batch, float x, float y, Color color, float size) {
        Texture whitePixel = assertsManager.getWhitePixel();
        batch.setColor(color);
        batch.draw(whitePixel, x - size / 2f, y - size / 2f, size, size);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
    }
}
