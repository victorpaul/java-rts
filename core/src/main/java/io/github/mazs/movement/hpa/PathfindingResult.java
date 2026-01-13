package io.github.mazs.movement.hpa;

import com.badlogic.gdx.math.Vector2;

import java.util.Collections;
import java.util.List;

public class PathfindingResult {
    private final List<Vector2> path;
    private final int nodesChecked;
    private final int nodesInOpenSet;
    private final float pathCost;
    private final boolean success;

    public PathfindingResult(List<Vector2> path, int nodesChecked, int nodesInOpenSet, float pathCost) {
        this.path = path;
        this.nodesChecked = nodesChecked;
        this.nodesInOpenSet = nodesInOpenSet;
        this.pathCost = pathCost;
        this.success = path != null && !path.isEmpty();
    }

    public static PathfindingResult failure(int nodesChecked, int nodesInOpenSet) {
        return new PathfindingResult(Collections.emptyList(), nodesChecked, nodesInOpenSet, 0f);
    }

    public List<Vector2> getPath() {
        return path;
    }

    public int getNodesChecked() {
        return nodesChecked;
    }

    public int getNodesInOpenSet() {
        return nodesInOpenSet;
    }

    public float getPathCost() {
        return pathCost;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "PathfindingResult{" +
            "success=" + success +
            ", pathLength=" + (path != null ? path.size() : 0) +
            ", nodesChecked=" + nodesChecked +
            ", nodesInOpenSet=" + nodesInOpenSet +
            ", pathCost=" + pathCost +
            '}';
    }
}
