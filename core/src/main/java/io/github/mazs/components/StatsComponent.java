package io.github.mazs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;

public class StatsComponent {
    private static final int SAMPLE_SIZE = 60;
    private static final long NANOS_TO_MILLIS = 1_000_000;

    private long frameStartTime;
    private long updateStartTime;
    private long renderStartTime;

    private float updateTimeMs;
    private float renderTimeMs;
    private float totalFrameTimeMs;
    private float fps;

    private final float[] updateTimeSamples = new float[SAMPLE_SIZE];
    private final float[] renderTimeSamples = new float[SAMPLE_SIZE];
    private final float[] frameTimeSamples = new float[SAMPLE_SIZE];
    private int sampleIndex = 0;

    private final BitmapFont font;
    private boolean enabled = true;

    public StatsComponent() {
        font = new BitmapFont();
        font.setColor(Color.YELLOW);
        font.getData().setScale(1.5f);
    }

    public void beginFrame() {
        if (!enabled) return;
        frameStartTime = TimeUtils.nanoTime();
    }

    public void beginUpdate() {
        if (!enabled) return;
        updateStartTime = TimeUtils.nanoTime();
    }

    public void endUpdate() {
        if (!enabled) return;
        long updateEndTime = TimeUtils.nanoTime();
        float updateMs = (updateEndTime - updateStartTime) / (float) NANOS_TO_MILLIS;

        updateTimeSamples[sampleIndex] = updateMs;
        updateTimeMs = calculateAverage(updateTimeSamples);
    }

    public void beginRender() {
        if (!enabled) return;
        renderStartTime = TimeUtils.nanoTime();
    }

    public void endRender() {
        if (!enabled) return;
        long renderEndTime = TimeUtils.nanoTime();
        float renderMs = (renderEndTime - renderStartTime) / (float) NANOS_TO_MILLIS;

        renderTimeSamples[sampleIndex] = renderMs;
        renderTimeMs = calculateAverage(renderTimeSamples);
    }

    public void endFrame() {
        if (!enabled) return;
        long frameEndTime = TimeUtils.nanoTime();
        float frameMs = (frameEndTime - frameStartTime) / (float) NANOS_TO_MILLIS;

        frameTimeSamples[sampleIndex] = frameMs;
        totalFrameTimeMs = calculateAverage(frameTimeSamples);

        fps = totalFrameTimeMs > 0 ? 1000f / totalFrameTimeMs : 0;

        sampleIndex = (sampleIndex + 1) % SAMPLE_SIZE;
    }

    private float calculateAverage(float[] samples) {
        float sum = 0;
        for (float sample : samples) {
            sum += sample;
        }
        return sum / samples.length;
    }

    public void render(SpriteBatch batch, float x, float y) {
        if (!enabled) return;

        font.draw(batch, String.format("FPS: %.1f", fps), x, y);
        font.draw(batch, String.format("Frame: %.2f ms", totalFrameTimeMs), x, y - 25);
        font.draw(batch, String.format("Update: %.2f ms", updateTimeMs), x, y - 50);
        font.draw(batch, String.format("Render: %.2f ms", renderTimeMs), x, y - 75);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public float getFps() {
        return fps;
    }

    public float getTotalFrameTimeMs() {
        return totalFrameTimeMs;
    }

    public float getUpdateTimeMs() {
        return updateTimeMs;
    }

    public float getRenderTimeMs() {
        return renderTimeMs;
    }

    public void dispose() {
        font.dispose();
    }
}
