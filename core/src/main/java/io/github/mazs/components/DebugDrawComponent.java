package io.github.mazs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DebugDrawComponent {
    private final AssertsManager assertsManager;
    private final List<DebugRectangle> rectangles = new ArrayList<>();
    private final List<DebugLine> lines = new ArrayList<>();

    private static class DebugRectangle {
        float x, y, width, height;
        Color color;
        float ttl;

        DebugRectangle(float x, float y, float width, float height, Color color, float ttl) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = new Color(color);
            this.ttl = ttl;
        }
    }

    private static class DebugLine {
        Vector2 from, to;
        Color color;
        float ttl;

        DebugLine(Vector2 from, Vector2 to, Color color, float ttl) {
            this.from = new Vector2(from);
            this.to = new Vector2(to);
            this.color = new Color(color);
            this.ttl = ttl;
        }
    }

    public DebugDrawComponent(AssertsManager assertsManager) {
        this.assertsManager = assertsManager;
    }

    public void update(float delta) {
        Iterator<DebugRectangle> rectIterator = rectangles.iterator();
        while (rectIterator.hasNext()) {
            DebugRectangle rect = rectIterator.next();
            rect.ttl -= delta;
        }
        rectangles.removeIf(rect -> rect.ttl <= 0);

        Iterator<DebugLine> lineIterator = lines.iterator();
        while (lineIterator.hasNext()) {
            DebugLine line = lineIterator.next();
            line.ttl -= delta;
        }
        lines.removeIf(line -> line.ttl <= 0);
    }

    public void render(SpriteBatch batch) {
        for (DebugRectangle rect : rectangles) {
            float drawX = rect.x-rect.width/2;
            float drawY = rect.y-rect.height/2;
            drawRectangleImmediate(
                batch,
                drawX,
                drawY,
                rect.width,
                rect.height, rect.color);
        }

        for (DebugLine line : lines) {
            drawLineImmediate(batch, line.from, line.to, line.color);
        }
    }

    public void drawPixel(SpriteBatch batch, float x, float y, Color color, float size) {
        Texture whitePixel = assertsManager.getWhitePixel();
        batch.setColor(color);
        batch.draw(whitePixel, x - size / 2f, y - size / 2f, size, size);
        batch.setColor(Color.WHITE);
    }

    public void drawRectangle(Vector2 xy, float size, Color color, float ttl) {
        rectangles.add(new DebugRectangle(xy.x, xy.y, size, size, color, ttl));
    }
    public void drawRectangle(float x, float y, float width, float height, Color color, float ttl) {
        rectangles.add(new DebugRectangle(x, y, width, height, color, ttl));
    }

    public void drawLine(Vector2 from, Vector2 to, Color color, float ttl) {
        lines.add(new DebugLine(from, to, color, ttl));
    }

    public void drawRectangleImmediate(SpriteBatch batch, float x, float y, float width, float height, Color color) {
        Texture whitePixel = assertsManager.getWhitePixel();
        batch.setColor(color);
        batch.draw(whitePixel, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawLineImmediate(SpriteBatch batch, Vector2 from, Vector2 to, Color color) {
        Texture whitePixel = assertsManager.getWhitePixel();

        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        float lineThickness = 2f;

        batch.setColor(color);
        batch.draw(whitePixel,
            from.x, from.y - lineThickness / 2f,
            0, lineThickness / 2f,
            distance, lineThickness,
            1, 1,
            angle,
            0, 0,
            1, 1,
            false, false);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
    }
}
