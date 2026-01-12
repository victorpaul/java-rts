package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Cluster {
    private final ClustersManager clustersManager;

    private List<Gate> gates = new LinkedList<>();

    private final int tileSize;
    private final int clusterCellsSize;
    private final Vector2 clusterPosition;

    public Cluster(ClustersManager clustersManager, int cellSize, int clusterCellsSize, Vector2 clusterPosition) {
        this.clustersManager = clustersManager;
        this.tileSize = cellSize;
        this.clusterCellsSize = clusterCellsSize;
        this.clusterPosition = clusterPosition;
    }

    public Vector2 getClusterCenter() {
        int clusterWorldSize = tileSize * clusterCellsSize;
        return new Vector2(
            clusterPosition.x * clusterWorldSize + clusterWorldSize / 2,
            clusterPosition.y * clusterWorldSize + clusterWorldSize / 2
        );
    }

    public void generateWalkableBordersSegments() {
        gates.clear();

        // bottom gates
        fillGates(gates, new Vector2(0, -tileSize),
            1, clusterCellsSize,
            1, 1);

        //top gates
        fillGates(gates, new Vector2(0, tileSize),
            1, clusterCellsSize,
            clusterCellsSize, clusterCellsSize);

        //left gates
        fillGates(gates, new Vector2(-tileSize, 0),
            1, 1,
            1, clusterCellsSize);

        //right gates
        fillGates(gates, new Vector2(tileSize, 0),
            clusterCellsSize, clusterCellsSize,
            1, clusterCellsSize);


    }

    private void fillGates(
        List<Gate> outGates,
        Vector2 clusterOffset,
        int fromX, int toX,
        int fromY, int toY) {
        UnitsSpatialHashGrid sg = clustersManager.getUnitsSpatialHashGrid();
        float bottomLeftX = clusterPosition.x * clusterCellsSize * tileSize + tileSize / 2f;
        float bottomLeftY = clusterPosition.y * clusterCellsSize * tileSize + tileSize / 2f;


        Gate tempGate = null;
        for (int x = fromX - 1; x < toX; x++) {
            for (int y = fromY - 1; y < toY; y++) {
                Vector2 inTile = new Vector2(
                    bottomLeftX + x * tileSize,
                    bottomLeftY + y * tileSize);
                Vector2 outTile = new Vector2(inTile).add(clusterOffset);

                Cluster neigbor = clustersManager.getClusterByTilePosition(outTile);
                boolean inTileWalkable = sg.findUnitAt(inTile.x, inTile.y) == null;
                boolean outTileWalkable = sg.findUnitAt(outTile.x, outTile.y) == null;

                if (neigbor != null && outTileWalkable && inTileWalkable) {
                    if (tempGate == null) {
                        tempGate = new Gate(new HashSet<>(), neigbor, clusterOffset);
                        outGates.add(tempGate);
                    }
                    tempGate.addTile(inTile);
                } else {
                    tempGate = null;
                }
            }
        }
    }

    private long tileToKey(Vector2 xy) {
        int cellX = (int) (xy.x / tileSize);
        int cellY = (int) (xy.y / tileSize);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    public void debug(DebugDrawComponent debugDrawComponent) {
        float clusterWorldSize = tileSize * clusterCellsSize * 0.95f;
        debugDrawComponent.drawRectangle(getClusterCenter(),
            clusterWorldSize, new Color(1f, 1f, 0f, 0.1f), 0.1f);

        gates.forEach(gate -> gate.debug(debugDrawComponent));

    }
}
