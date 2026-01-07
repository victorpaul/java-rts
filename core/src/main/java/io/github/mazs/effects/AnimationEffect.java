package io.github.mazs.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Represents a temporary visual effect
// well... it works
public abstract class AnimationEffect {
    private final Texture texture;
    private final float x;
    private final float y;
    private float lifetime;
    private float maxLifetime;
    private boolean finished;

    public AnimationEffect(Texture texture, float x, float y, float duration) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.maxLifetime = duration;
        this.lifetime = 0f;
        this.finished = false;
    }

    /**
     * Updates the animation effect.
     *
     * @param delta Time elapsed since last frame
     */
    public void update(float delta) {
        lifetime += delta;
        if (lifetime >= maxLifetime) {
            finished = true;
        }
    }

    /**
     * Renders the animation effect.
     *
     * @param batch SpriteBatch to render with
     */
    public void render(SpriteBatch batch) {
        if (!finished) {
            // Calculate fade out alpha based on remaining lifetime
            float alpha = 1f - (lifetime / maxLifetime);

            final float renderSize = 32;

            float drawX = x - renderSize / 2f;
            float drawY = y - renderSize / 2f;

            // Draw with fade out effect
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(texture, drawX, drawY, renderSize, renderSize);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void dispose() {

    }
}
