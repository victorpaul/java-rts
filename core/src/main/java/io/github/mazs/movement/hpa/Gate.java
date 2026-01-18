package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Gate {
    private final List<Vector2> tiles;
    private final Cluster neighborCluster;
    private final Vector2 direction; // the direction in which we have external gate from neighbor cluster
    private final Map<Gate, Float> reachableGates = new HashMap<>(); // gates reachable from this gate with their path costs

    public Gate(List<Vector2> tiles, Cluster cluster, Vector2 direction) {
        this.tiles = tiles;
        this.neighborCluster = cluster;
        this.direction = direction;
    }

    public Vector2 getMiddlePoint() {
        if (tiles.isEmpty()) {
            return null;
        }

        // Get middle tile
        int middleIndex = tiles.size() / 2;
        return tiles.get(middleIndex);
    }

    void addTile(Vector2 tile) {
        tiles.add(tile);
    }

    //todo, cache neighbor gate
    public Gate getNeighborGate(ClustersManager clustersManager) {
        Vector2 neighborGatePosition = new Vector2(getMiddlePoint()).add(direction);
        Cluster neighborCluster = clustersManager.getClusterByTilePosition(neighborGatePosition);
        if (neighborCluster != null) {
            return neighborCluster.getGateByPosition(neighborGatePosition);
        }
        return null;
    }


    public List<Vector2> getTiles() {
        return tiles;
    }

    public Map<Gate, Float> getReachableGates() {
        return reachableGates;
    }

    public void addReachableGate(Gate gate, float pathCost) {
        reachableGates.put(gate, pathCost);
    }

    public void clearReachableGates() {
        reachableGates.clear();
    }

    void debug() {
        tiles.forEach(tile -> {
            DebugDrawComponent.getInstance().drawRectangle(tile, 4, Color.BLUE, 0.1f);
        });
        DebugDrawComponent.getInstance().drawRectangle(getMiddlePoint(), 6, Color.BLUE, 0.1f);
    }

    public Cluster getNeighborCluster() {
        return neighborCluster;
    }
}
