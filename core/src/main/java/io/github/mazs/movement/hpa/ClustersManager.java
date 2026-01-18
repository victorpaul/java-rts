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

        return Optional.ofNullable(getClusterByTilePosition(position))
            .map(c -> c.getGateByPosition(position))
            .map(g -> {

                if (g.getNeighborCluster() == getClusterByTilePosition(end)) {
                    Gate neighborGate = g.getNeighborGate(this);
                    PathfindingResult result = AStarPathfinder.findPath(
                        g.getNeighborCluster(), neighborGate.getMiddlePoint(), end);
                    return result.isSuccess();

                }
                return false;
            })
            .orElseGet(() -> false);
    }

    @Override
    public void addToClosedSet(Set<Long> closedSet, Vector2 position) {
        // todo, improve this one if performance issue
        // stupid workaround to mark node as passed, as in this case node is cluster gate
        // and it is uniq only in range of the same cluster, gate from another side of neighbor is logically the same node,
        // but it has different position
        // so, workaround to avoid passing same gates twice, it to add to closed set bot gates that form node

        // find cluster where this gate(node) is
        Optional.ofNullable(getClusterByTilePosition(position))
            // find gate object
            .map(cluster -> cluster.getGateByPosition(position))
            // extract gate on another side in neighbor cluster, which connected to this one
            .map(gate -> gate.getNeighborGate(this))
            // also add it to closed set
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

        Optional<Gate> neighborGate = Optional
            .ofNullable(cluster.getGateByPosition(location))
            .map(gate -> gate.getNeighborGate(this));

        // we get local gate in cluster
        // get neighbor gate from neighbor cluster
        // and find reachable gates from neighbor cluster
        return neighborGate.map(gate -> gate.getReachableGates().keySet().stream()
                .map(Gate::getMiddlePoint)
                .collect(Collectors.toList()))

            // we are somewhere in the cluster
            // lets calculate gates we can reach
            .orElseGet(() -> cluster.getGates().stream().map(g -> {
                    PathfindingResult pathResult = AStarPathfinder.findPath(
                        cluster,
                        location,
                        g.getMiddlePoint()
                    );
                    return pathResult.isSuccess()
                        ? g
                        : null;
                })
                .filter(Objects::nonNull)
                .map(Gate::getMiddlePoint)
                .collect(Collectors.toList()));
    }

    @Override
    public float getCost(Vector2 from, Vector2 to) {
        Cluster clusterFrom = getClusterByTilePosition(from);
        Gate gateFrom = clusterFrom.getGateByPosition(from);

        // we calculate gate to gate cost
        if (gateFrom != null) {
            return Optional.ofNullable(gateFrom.getNeighborGate(this))
                .map(gateA -> {
                    Gate gateB = getGate(to);
                    return gateA
                        .getReachableGates()
                        .get(gateB);
                })
                .orElseGet(() -> {
                    DebugDrawComponent
                        .getInstance()
                        .drawLine(from, to, Color.RED, 10f);
                    return Float.MAX_VALUE;
                });
        } else {
            // we calculate cost from somewhere in the cluster to gate
            PathfindingResult result = AStarPathfinder.findPath(clusterFrom, from, to);
            if (result.isSuccess()) {
                return result.getPathCost();
            }
        }

        return Float.MAX_VALUE;
    }

    private Gate getGate(Vector2 to) {
        return Optional
            .ofNullable(getClusterByTilePosition(to))
            .map(c -> c.getGateByPosition(to))
            .orElse(null);
    }

    @Override
    public float getHeuristic(Vector2 from, Vector2 to) {
        return from.dst(to);
    }
}
