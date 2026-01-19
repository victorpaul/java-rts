package io.github.mazs.units;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.movement.Moving;
import io.github.mazs.movement.PatrolComponent;
import io.github.mazs.movement.UnitMovementComponent;
import io.github.mazs.worlds.WorldRts;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Unit implements Moving {
    private static final String IDLE_SPRITE_PATH = "TinySwords/Units/Blue Units/Pawn/Pawn_Idle.png";
    private static final String RUN_SPRITE_PATH = "TinySwords/Units/Blue Units/Pawn/Pawn_Run.png";
    private static final int FRAME_WIDTH = 192;
    private static final int FRAME_HEIGHT = 192;

    private final UnitMovementComponent movementComponent;
    private final PatrolComponent patrolComponent;

    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> runAnimation;

    public Pawn(WorldRts world, Vector2 spawnPosition) {
        super(world, new Vector2(spawnPosition), 64, 30);
        this.movementComponent = new UnitMovementComponent(this, 100);
        this.patrolComponent = new PatrolComponent(this, movementComponent);

        idleAnimation = createAnimation(
            IDLE_SPRITE_PATH,
            FRAME_WIDTH,
            FRAME_HEIGHT,
            8, 15);
        runAnimation = createAnimation(
            RUN_SPRITE_PATH,
            FRAME_WIDTH,
            FRAME_HEIGHT,
            6, 15);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        movementComponent.update(delta);
        patrolComponent.update(delta);
    }

    public UnitMovementComponent getMovementComponent() {
        return movementComponent;
    }

    @Override
    public void moveTo(float x, float y) {
        movementComponent.moveTo(x, y);
    }

    @Override
    public void patrol(Vector2 to) {
        List<Vector2> points = new ArrayList<>();
        points.add(getPosition());
        points.add(to);
        patrolComponent.setPatrolPoints(points);
    }

    @Override
    protected Animation<TextureRegion> getCurrentAnimation() {
        if (movementComponent.isMoving(position)) return runAnimation;
        return idleAnimation;
    }
}
