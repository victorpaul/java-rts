package io.github.mazs.movement.hpa;

import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Set;

import static io.github.mazs.movement.hpa.TileUtils.positionToKey;

public interface PathfindingGraph {
    List<Vector2> getNeighbors(Vector2 node);
    float getCost(Vector2 from, Vector2 to);
    float getHeuristic(Vector2 from, Vector2 to);

    default boolean isGoalReached(Vector2 position, Vector2 end) {
        return position.epsilonEquals(end, 0.1f);
    }

    default void addToClosedSet(Set<Long> closedSet, Vector2 position) {
        closedSet.add(positionToKey(position));
    }
}
