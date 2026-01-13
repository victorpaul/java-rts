package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.units.Unit;

public class HpaPathFindingStrategy implements IMovementStrategy {

    @Override
    public void resetState() {

    }

    @Override
    public Vector2 calculateNextCell(Unit owner, Vector2 targetFinalPosition) {

        ok, here use AStarPathfinder to feed cluster manger to find global path
        and then use current cluster to calculate local path from nodes to nodes in realtime

        keep path in memory unless you faced obstacle, BTW, global path finding, should ignore units

        Also,if unit died, or destroyed, we may need to mark cluster as dirty, to recalculate links between gates

        return null;
    }
}
