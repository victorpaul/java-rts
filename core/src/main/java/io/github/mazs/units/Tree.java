package io.github.mazs.units;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.mazs.worlds.WorldRts;

public class Tree extends Unit {
    private static final String TEXTURE_PATH = "TinySwords/Terrain/Resources/Wood/Trees/Tree1.png";
    private final Animation<TextureRegion> idleAnimation;

    public Tree(WorldRts world, float x, float y) {
        super(world, x, y,
            128,
            20);
        idleAnimation = createAnimation(
            TEXTURE_PATH,
            1536 / 8,
            256,
            8,
            5
        );
    }

    @Override
    protected Animation<TextureRegion> getCurrentAnimation() {
        return idleAnimation;
    }

}
