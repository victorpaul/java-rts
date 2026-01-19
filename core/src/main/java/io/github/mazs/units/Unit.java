package io.github.mazs.units;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.worlds.WorldRts;

// Represents something that may be placed in the world, and it has collision
public abstract class Unit {
    protected WorldRts world;
    protected Vector2 position;
    protected float lifetime = 0f;
    protected final int renderSize;
    protected final int renderYOffset;
    private boolean pendingDestroy = false;

    public Unit(WorldRts world, Vector2 position, int renderSize, int renderYOffset) {
        this.world = world;
        this.position = position    ;
        this.renderSize = renderSize;
        this.renderYOffset = renderYOffset;

        world.getSpatialGrid().snapToGrid(position);
        world.getSpatialGrid().update(this);
    }

    protected Animation<TextureRegion> createAnimation(
        String spriteSheetPath,
        int frameWidth,
        int frameHeight,
        int frameCount,
        float fps) {

        Texture spriteSheet = world.assertsManager.getTexture(spriteSheetPath);

        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }

        return new Animation<>(1f / fps, frames);
    }

    public void update(float delta) {
        lifetime += delta;
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> currentAnimation = getCurrentAnimation();

        if (currentAnimation != null) {
            TextureRegion currentFrame = currentAnimation.getKeyFrame(lifetime, true);

            float renderX = position.x - renderSize / 2f;
            float renderY = position.y - renderYOffset;

            batch.draw(currentFrame, renderX, renderY, renderSize, renderSize);
        }
    }

    protected abstract Animation<TextureRegion> getCurrentAnimation();

    public Vector2 getPosition() {
        return position;
    }

    public WorldRts getWorld() {
        return world;
    }

    public void destroy() {
        pendingDestroy = true;
    }

    public boolean isPendingDestroy() {
        return pendingDestroy;
    }

    public void drawDebug(SpriteBatch batch) {
        world.getDebugDraw().drawPixel(batch, position.x, position.y, Color.GREEN, 4f);
    }

    public void dispose() {
        world.getSpatialGrid().remove(this);
    }

    public void updateSpatialPosition() {
        world.getSpatialGrid().update(this);
    }
}
