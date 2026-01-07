package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.UnitsSpatialHashGrid;
import io.github.mazs.units.Unit;
import io.github.mazs.worlds.WorldRts;

public class UnitMovementComponent {
    private Unit owner;
    private final Vector2 targetPosition;
    private float movementSpeed;
    private static final float ARRIVAL_THRESHOLD = 2f;

    private final Vector2 tempDirection = new Vector2();
    private final Vector2 tempCheckPosition = new Vector2();

    public UnitMovementComponent(Unit owner, float movementSpeed) {
        this.owner = owner;
        this.movementSpeed = movementSpeed;
        this.targetPosition = new Vector2(owner.getPosition());
    }

    public void update(float delta) {
        Vector2 position = owner.getPosition();
        float distanceToTarget = position.dst(targetPosition);

        if (distanceToTarget > ARRIVAL_THRESHOLD) {
            tempDirection.set(targetPosition).sub(position).nor();
            float moveDistance = movementSpeed * delta;

            // Check collision at half cell size ahead in movement direction
            float halfCellSize = WorldRts.TILE_SIZE / 2f;
            tempCheckPosition.set(position).add(tempDirection.x * halfCellSize, tempDirection.y * halfCellSize);

            UnitsSpatialHashGrid grid = owner.getWorld().getSpatialGrid();
            Unit unitAtCheck = grid.findUnitAt(tempCheckPosition.x, tempCheckPosition.y);

            if (unitAtCheck != null && unitAtCheck != owner) {
                moveTo(position.x, position.y);
                return;
            }

            if (moveDistance > distanceToTarget) {
                position.set(targetPosition);
            } else {
                position.add(tempDirection.scl(moveDistance));
            }
            owner.updateSpatialPosition();
        }
    }

    public void moveTo(float x, float y) {
        targetPosition.set(x, y);
        owner.getWorld().getSpatialGrid().snapToGrid(targetPosition);
    }

    public boolean isMoving(Vector2 currentPosition) {
        return currentPosition.dst(targetPosition) > ARRIVAL_THRESHOLD;
    }

}
