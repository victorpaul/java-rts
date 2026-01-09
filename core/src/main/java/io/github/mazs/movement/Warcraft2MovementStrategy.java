package io.github.mazs.movement;

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

    // Configuration constants
    private static final float ANGLE_STEP = 45f;
    private static final int MAX_ANGLE_STEPS = 3; // 45 * 3 = 135 degrees
    private static final float COMMITMENT_ANGLE_THRESHOLD = 90f;
    private static final float BACKTRACK_EPSILON = 0.1f;

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

        // Try direct path to target
        Vector2 directionToTarget = calculateDirection(currentPosition, targetFinalPosition);
        Vector2 directTile = getTileInDirection(owner, currentPosition, directionToTarget, 0);

        Vector2 resultTile;

        if (!isTileBlocked(owner, directTile)) {
            // Direct path is clear
            commitment = Commitment.NO_COMMITMENT;
            resultTile = directTile;
        } else {
            // Obstacle in the way - navigate around it
            Vector2 directionToObstacle = calculateDirection(currentPosition, directTile);
            resultTile = avoidObstacle(owner, currentPosition, directionToObstacle, targetFinalPosition);
        }

        // Track position for next frame
        lastPosition.set(currentPosition);
        sg.snapToGrid(lastPosition);

        return resultTile;
    }

    private Vector2 avoidObstacle(Unit owner, Vector2 currentPosition, Vector2 directionToTarget, Vector2 targetFinalPosition) {
        if (commitment == Commitment.NO_COMMITMENT) {
            return handleNoCommitment(owner, currentPosition, directionToTarget, targetFinalPosition);
        }

        Vector2 directionAlongCommitment = calculateDirection(lastPosition, currentPosition);
        return handleCommitment(owner, currentPosition, directionAlongCommitment);
    }

    private Vector2 handleNoCommitment(Unit owner, Vector2 currentPosition, Vector2 directionToTarget, Vector2 targetFinalPosition) {
        for (int d = (int) ANGLE_STEP; d <= (int) (ANGLE_STEP * MAX_ANGLE_STEPS); d += (int) ANGLE_STEP) {
            Vector2 leftTile = getTileInDirection(owner, currentPosition, directionToTarget, d);
            Vector2 rightTile = getTileInDirection(owner, currentPosition, directionToTarget, -d);

            boolean leftFree = !isTileBlocked(owner, leftTile);
            boolean rightFree = !isTileBlocked(owner, rightTile);

            // If both are free, choose the one closer to target
            if (leftFree && rightFree) {
                float distanceToLeft = leftTile.dst(targetFinalPosition);
                float distanceToRight = rightTile.dst(targetFinalPosition);

                boolean chooseLeft = distanceToLeft < distanceToRight;
                setCommitmentIfSharpTurn(d, chooseLeft ? Commitment.TURN_LEFT : Commitment.TURN_RIGHT);
                return chooseLeft ? leftTile : rightTile;
            }

            // If only right is free
            if (rightFree) {
                setCommitmentIfSharpTurn(d, Commitment.TURN_RIGHT);
                return rightTile;
            }

            // If only left is free
            if (leftFree) {
                setCommitmentIfSharpTurn(d, Commitment.TURN_LEFT);
                return leftTile;
            }
        }
        return currentPosition;
    }

    private void setCommitmentIfSharpTurn(float angle, Commitment newCommitment) {
        if (angle >= COMMITMENT_ANGLE_THRESHOLD) {
            commitment = newCommitment;
        }
    }

    private Vector2 handleCommitment(Unit owner, Vector2 currentPosition, Vector2 directionToTarget) {
        float[] anglesToCheck = new float[]{ANGLE_STEP, COMMITMENT_ANGLE_THRESHOLD, 0f};

        for (float angle : anglesToCheck) {
            Vector2 followTile = getTileInDirection(owner, currentPosition, directionToTarget,
                commitment == Commitment.TURN_LEFT ? -angle : angle
            );

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
        boolean isBacktracking = lastPosition.epsilonEquals(tilePosition, BACKTRACK_EPSILON);

        return hasUnit || isBacktracking;
    }

}
