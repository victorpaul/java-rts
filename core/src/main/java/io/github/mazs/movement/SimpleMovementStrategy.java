package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.movement.hpa.TileUtils;
import io.github.mazs.units.Unit;
import io.github.mazs.worlds.WorldRts;

/**
 * Simple movement strategy that calculates the next tile in the direction
 * of the final destination without any pathfinding or obstacle avoidance.
 */
public class SimpleMovementStrategy implements IMovementStrategy {

    private final Vector2 currentDirection = new Vector2();
    private final Vector2 nextPosition = new Vector2();

    @Override
    public void resetState() {

    }

    @Override
    public Vector2 calculateNextCell(Unit owner, Vector2 targetFinalPosition) {
        Vector2 currentPosition = owner.getPosition();

        // Calculate direction to target
        currentDirection.set(targetFinalPosition).sub(currentPosition).nor();

        float moveDistance = WorldRts.TILE_SIZE;
        nextPosition.set(currentPosition).add(currentDirection.scl(moveDistance));

        // Snap to grid to get the tile center
        TileUtils.snapToTileCenterInPlace(nextPosition);

        return nextPosition;
    }
}
