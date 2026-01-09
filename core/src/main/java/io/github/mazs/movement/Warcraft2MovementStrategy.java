package io.github.mazs.movement;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.UnitsSpatialHashGrid;
import io.github.mazs.units.Unit;

import static io.github.mazs.worlds.WorldRts.TILE_SIZE;

/**
 * Warcraft 2 style movement strategy with obstacle avoidance.
 * Uses wall-following algorithm: when blocked, commits to going left or right
 * around the obstacle until a direct path is available again.
 */
public class Warcraft2MovementStrategy implements IMovementStrategy {

    private enum Commitment {
        NO_COMMITMENT,
        TURN_LEFT,
        TURN_RIGHT
    }

    private Vector2 lastPosition = new Vector2();
    private Commitment commitment = Commitment.NO_COMMITMENT;

    @Override
    public void resetState() {
        commitment = Commitment.NO_COMMITMENT;
    }

    @Override
    public Vector2 calculateNextCell(Unit owner, Vector2 targetFinalPosition) {
        UnitsSpatialHashGrid sg = owner.getWorld().getSpatialGrid();
        Vector2 currentPosition = owner.getPosition();

        Vector2 directionToTarget = calculateDirection(currentPosition, targetFinalPosition);
        Vector2 nextTile = getTileInDirection(owner, currentPosition, directionToTarget, 0);

        Vector2 resultTile;

        // Step 2: If it is free, return it
        if (!isTileBlocked(owner, nextTile)) {
            commitment = Commitment.NO_COMMITMENT;
            resultTile = nextTile;
        } else {
            // Calculate direction to the obstacle, not the far target
            Vector2 directionToObstacle = calculateDirection(currentPosition, nextTile);
            resultTile = avoidObstacle(owner, currentPosition, directionToObstacle);
        }

        // Update lastPosition only here, before returning
        lastPosition.set(currentPosition);
        sg.snapToGrid(lastPosition);
        owner.getWorld().getDebugDraw().drawRectangle(
            lastPosition, TILE_SIZE * 0.9f,
            new Color(0, 0, 1, 0.3f), 0.4f);

        return resultTile;
    }

    private Vector2 avoidObstacle(Unit owner, Vector2 currentPosition, Vector2 directionToTarget) {
        if (commitment == Commitment.NO_COMMITMENT) {
            return handleNoCommitment(owner, currentPosition, directionToTarget);
        }

        Vector2 directionAlongCommitment = calculateDirection(lastPosition, currentPosition);
        return handleCommitment(owner, currentPosition, directionAlongCommitment);
    }

    private Vector2 handleNoCommitment(Unit owner, Vector2 currentPosition, Vector2 directionToTarget) {
        for (int d = 45; d <= (45 * 3); d += 45) {
            Vector2 leftTile = getTileInDirection(owner, currentPosition, directionToTarget, d);
            Vector2 rightTile = getTileInDirection(owner, currentPosition, directionToTarget, -d);

            float debugTime = 3;
            // debug directions
            owner.getWorld().getDebugDraw().drawLine(currentPosition, leftTile, Color.ORANGE, debugTime);
            owner.getWorld().getDebugDraw().drawLine(currentPosition, rightTile, Color.YELLOW, debugTime);

            // debug collision
            owner.getWorld().getDebugDraw().drawRectangle(leftTile, TILE_SIZE * 0.5f,
                isTileBlocked(owner, leftTile) ? Color.RED : Color.GREEN,
                debugTime);
            owner.getWorld().getDebugDraw().drawRectangle(rightTile, TILE_SIZE * 0.5f,
                isTileBlocked(owner, rightTile) ? Color.RED : Color.GREEN,
                debugTime);

            if (!isTileBlocked(owner, rightTile)) {
                if (d >= 90) {
                    commitment = Commitment.TURN_RIGHT;
                }
                return rightTile;
            }

            if (!isTileBlocked(owner, leftTile)) {
                if (d >= 90) {
                    commitment = Commitment.TURN_LEFT;
                }
                return leftTile;
            }


        }
        return currentPosition;
    }

    private Vector2 handleCommitment(Unit owner, Vector2 currentPosition, Vector2 directionToTarget) {
        float[] anglesToCheck = new float[]{45f, 90f, 0f};

        for (int i = 0; i < anglesToCheck.length; i++) {
            float d = anglesToCheck[i];
            Vector2 followTile = getTileInDirection(owner, currentPosition, directionToTarget,
                commitment == Commitment.TURN_LEFT ? -d : d
            );

            float debugTime = 3;
            // debug directions
            owner.getWorld().getDebugDraw().drawLine(currentPosition, followTile, Color.BROWN, debugTime);

            // debug collision
            owner.getWorld().getDebugDraw().drawRectangle(followTile, TILE_SIZE * 0.5f,
                isTileBlocked(owner, followTile) ? Color.RED : Color.GREEN,
                debugTime);


            if (!isTileBlocked(owner, followTile)) {
                return followTile;
            }


        }
        commitment = Commitment.NO_COMMITMENT;
        return currentPosition;
    }


    private Vector2 calculateDirection(Vector2 from, Vector2 to) {
        return new Vector2(to).sub(from).nor();
    }

    private Vector2 getTileInDirection(Unit owner, Vector2 currentPosition, Vector2 direction, float degrees) {
        Vector2 rotatedDirection = new Vector2(direction).rotateDeg(degrees);
        float moveDistance = TILE_SIZE;
        Vector2 tilePosition = new Vector2(currentPosition).add(rotatedDirection.x * moveDistance, rotatedDirection.y * moveDistance);
        owner.getWorld().getSpatialGrid().snapToGrid(tilePosition);
        return tilePosition;
    }

    private boolean isTileBlocked(Unit owner, Vector2 tilePosition) {
        Unit unitAtTile = owner.getWorld().getSpatialGrid().findUnitAt(tilePosition.x, tilePosition.y);
        boolean hasUnit = unitAtTile != null && unitAtTile != owner;

        // Prevent moving back to the exact tile we just came from
        boolean isBacktracking = lastPosition.epsilonEquals(tilePosition, 0.1f);

        return hasUnit || isBacktracking;
    }

}
