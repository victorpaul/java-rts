package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.units.Unit;

public class UnitMovementComponent {
    private Unit owner;
    private final Vector2 finalTargetPosition;
    private final Vector2 currentTargetTile;
    private float movementSpeed;
    private static final float ARRIVAL_THRESHOLD = 2f;
    private IMovementStrategy movementStrategy;

    private final Vector2 tempDirection = new Vector2();

    public UnitMovementComponent(Unit owner, float movementSpeed) {
        this.owner = owner;
        this.movementSpeed = movementSpeed;
        this.finalTargetPosition = new Vector2(owner.getPosition());
        this.currentTargetTile = new Vector2(owner.getPosition());
        this.movementStrategy = new Warcraft2MovementStrategy();
    }

    public void update(float delta) {
        Vector2 unitPosition = owner.getPosition();

        // Step 1: If we haven't reached current target tile, move towards it
        if (!hasReachedTile(unitPosition, currentTargetTile)) {
            moveTowardsTile(delta);
            return;
        }

        // Step 2: We reached current tile, check if it's the final destination
        if (hasReachedTile(unitPosition, finalTargetPosition)) {
            return; // Arrived at final destination
        }

        // Step 3: Calculate next tile using movement strategy
        Vector2 nextTile = movementStrategy.calculateNextCell(owner, finalTargetPosition);
        currentTargetTile.set(nextTile);
    }

    private boolean hasReachedTile(Vector2 position, Vector2 targetTile) {
        return position.dst(targetTile) <= ARRIVAL_THRESHOLD;
    }

    private void moveTowardsTile(float delta) {
        Vector2 position = owner.getPosition();
        tempDirection.set(currentTargetTile).sub(position).nor();

        float moveDistance = movementSpeed * delta;
        float distanceToTarget = position.dst(currentTargetTile);

        if (moveDistance > distanceToTarget) {
            position.set(currentTargetTile);
        } else {
            position.add(tempDirection.scl(moveDistance));
        }
        owner.updateSpatialPosition();
    }

    public void moveTo(float x, float y) {
        movementStrategy.resetState();
        finalTargetPosition.set(x, y);
        owner.getWorld().getSpatialGrid().snapToGrid(finalTargetPosition);
        // Reset current tile to current position to trigger recalculation
        currentTargetTile.set(owner.getPosition());
    }

    public boolean isMoving(Vector2 currentPosition) {
        return currentPosition.dst(finalTargetPosition) > ARRIVAL_THRESHOLD;
    }

}
