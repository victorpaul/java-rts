package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;

import java.util.*;
import java.util.stream.Collectors;

public class ClustersManager implements PathfindingGraph {

    private final Map<Long, Cluster> clustersMap;
    private final int cellSize;
    private final int clusterCellsSize;
    private final UnitsSpatialHashGrid unitsSpatialHashGrid;
    private final Vector2 mapSize;

    public ClustersManager(int cellSize, int clusterCellsSize, UnitsSpatialHashGrid unitsSpatialHashGrid, Vector2 mapSize) {
        this.cellSize = cellSize;
        this.clusterCellsSize = clusterCellsSize;
        this.unitsSpatialHashGrid = unitsSpatialHashGrid;
        this.mapSize = mapSize;
        this.clustersMap = new HashMap<>();
    }

    public void generateClusters() {
        clustersMap.clear();

        int clusterWorldSize = cellSize * clusterCellsSize;

        int clustersInX = (int) Math.ceil(mapSize.x / clusterWorldSize);
        int clustersInY = (int) Math.ceil(mapSize.y / clusterWorldSize);

        // First: Create all clusters
        for (int clusterX = 0; clusterX < clustersInX; clusterX++) {
            for (int clusterY = 0; clusterY < clustersInY; clusterY++) {
                Vector2 clusterPosition = new Vector2(clusterX, clusterY);
                Cluster cluster = new Cluster(this, cellSize, clusterCellsSize, clusterPosition);

                long key = getClusterKey(clusterX, clusterY);
                clustersMap.put(key, cluster);
            }
        }

        // Generate gates for all clusters with interlinks
        for (Cluster cluster : clustersMap.values()) {
            cluster.generateGates();
            cluster.calculateGatesLinks();
        }
    }

    private long getClusterKey(int clusterX, int clusterY) {
        return ((long) clusterX << 32) | (clusterY & 0xFFFFFFFFL);
    }

    public UnitsSpatialHashGrid getUnitsSpatialHashGrid() {
        return unitsSpatialHashGrid;
    }

    public Cluster getClusterByTilePosition(Vector2 xy) {
        return getClusterByTilePosition(xy.x, xy.y);
    }

    public Cluster getClusterByTilePosition(float tileX, float tileY) {
        int clusterWorldSize = cellSize * clusterCellsSize;
        int clusterX = (int) (tileX / clusterWorldSize);
        int clusterY = (int) (tileY / clusterWorldSize);

        long key = getClusterKey(clusterX, clusterY);
        return clustersMap.get(key);
    }

    public void debug() {
        clustersMap.forEach((key, cluster) -> cluster.debug());
    }

    @Override
    public boolean isGoalReached(Vector2 position, Vector2 end) {
        Cluster currentCluster = getClusterByTilePosition(position);
        Cluster goalCluster = getClusterByTilePosition(end);
        return currentCluster == goalCluster;
    }

    @Override
    public void addToClosedSet(Set<Long> closedSet, Vector2 position) {
        // todo, improve this one if performance issue
        // stupid workaround to mark node as passed, as node is made of two gates actually
        // that are on the edges of cluster touching each another
        Optional.of(getClusterByTilePosition(position))
            .map(cluster -> cluster.getGateByPosition(position))
            .map(gate -> gate.getNeighborGate(this))
            .ifPresent(neighborGate -> {
                PathfindingGraph.super.addToClosedSet(closedSet, neighborGate.getMiddlePoint());
            });

        PathfindingGraph.super.addToClosedSet(closedSet, position);
    }

    @Override
    public List<Vector2> getNeighbors(Vector2 location) {
        Cluster cluster = getClusterByTilePosition(location);

        // Check if location is within valid cluster bounds
        if (cluster == null) {
            return new LinkedList<>();
        }

        Gate gate = cluster.getGateByPosition(location);
        if (gate != null) {
            // we got gates by location, return gates we can reach from neighbor clusters
            return gate.getReachableGates().keySet().stream()
                .map(g -> g.getNeighborGate(this))
                .filter(Objects::nonNull)
                .map(Gate::getMiddlePoint)
                .collect(Collectors.toList());
        } else {
            // we are somewhere in the cluster
            // lets calculate gates we can reach in the cluster and convert them to neighbor gates location
            return cluster.getGates().stream().map(g -> {
                    PathfindingResult pathResult = AStarPathfinder.findPath(
                        cluster,
                        location,
                        g.getMiddlePoint()
                    );
                    return pathResult.isSuccess()
                        ? g.getNeighborGate(this)
                        : null;
                })
                .filter(Objects::nonNull)
                .map(Gate::getMiddlePoint)
                .collect(Collectors.toList());
        }
    }

    @Override
    public float getCost(Vector2 from, Vector2 to) {
        return from.dst(to);
    }

    @Override
    public float getHeuristic(Vector2 from, Vector2 to) {
        return from.dst(to);
    }
}
