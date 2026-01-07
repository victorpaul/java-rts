package io.github.mazs.units;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.mazs.movement.Moving;
import io.github.mazs.movement.UnitMovementComponent;
import io.github.mazs.worlds.WorldRts;

public class Pawn extends Unit implements Moving {
    private static final String IDLE_SPRITE_PATH = "TinySwords/Units/Blue Units/Pawn/Pawn_Idle.png";
    private static final String RUN_SPRITE_PATH = "TinySwords/Units/Blue Units/Pawn/Pawn_Run.png";
    private static final int FRAME_WIDTH = 192;
    private static final int FRAME_HEIGHT = 192;

    private UnitMovementComponent movementComponent;

    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> runAnimation;

    public Pawn(WorldRts world, float x, float y) {
        super(world, x, y,
            64,
            30);
        this.movementComponent = new UnitMovementComponent(this, 100f);

        idleAnimation = createAnimation(
            IDLE_SPRITE_PATH,
            FRAME_WIDTH,
            FRAME_HEIGHT,
            8,
            15
        );

        runAnimation = createAnimation(
            RUN_SPRITE_PATH,
            FRAME_WIDTH,
            FRAME_HEIGHT,
            6,
            15
        );
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        movementComponent.update(delta);
    }

    public UnitMovementComponent getMovementComponent() {
        return movementComponent;
    }

    @Override
    public void moveTo(float x, float y) {
        movementComponent.moveTo(x, y);
    }

    @Override
    protected Animation<TextureRegion> getCurrentAnimation() {
        if (movementComponent.isMoving(position)) return runAnimation;
        return idleAnimation;
    }
}
