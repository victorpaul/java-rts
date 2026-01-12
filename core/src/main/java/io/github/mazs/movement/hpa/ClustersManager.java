package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;

import java.util.HashMap;
import java.util.Map;

public class ClustersManager {

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

        // Second: Generate gates for all clusters (now all neighbors exist)
        for (Cluster cluster : clustersMap.values()) {
            cluster.generateWalkableBordersSegments();
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

    public void debug(DebugDrawComponent debugDrawComponent) {
        clustersMap.values().forEach(cluster -> {
            cluster.debug(debugDrawComponent);
        });
    }

}
