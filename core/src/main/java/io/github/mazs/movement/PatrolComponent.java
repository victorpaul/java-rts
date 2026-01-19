package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class PatrolComponent {
    private final Unit owner;
    private final UnitMovementComponent movementComponent;
    private final List<Vector2> patrolPoints = new ArrayList<>();
    private int currentPointIndex = 0;

    public PatrolComponent(Unit owner, UnitMovementComponent movementComponent) {
        this.owner = owner;
        this.movementComponent = movementComponent;
    }

    public void update(float delta) {
        if (patrolPoints.isEmpty()) {
            return;
        }

        // Check if unit has reached current patrol point
        if (!movementComponent.isMoving(owner.getPosition())) {
            // Move to next patrol point
            currentPointIndex = (currentPointIndex + 1) % patrolPoints.size();
            Vector2 nextPoint = patrolPoints.get(currentPointIndex);
            movementComponent.moveTo(nextPoint.x, nextPoint.y);
        }
    }

    public void setPatrolPoints(List<Vector2> points) {
        patrolPoints.clear();
        if (points == null || points.isEmpty()) {
            return;
        }

        for (Vector2 point : points) {
            patrolPoints.add(new Vector2(point));
        }

        currentPointIndex = 0;
        Vector2 firstPoint = patrolPoints.get(0);
        movementComponent.moveTo(firstPoint.x, firstPoint.y);
    }

}
