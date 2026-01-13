package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cluster implements PathfindingGraph {
    private final ClustersManager clustersManager;

    private List<Gate> gates = new LinkedList<>();
    private Map<Long, Gate> tileToGateMap = new HashMap<>();

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
            clusterPosition.x * clusterWorldSize + clusterWorldSize / 2f,
            clusterPosition.y * clusterWorldSize + clusterWorldSize / 2f
        );
    }

    public void generateGates() {
        gates.clear();
        tileToGateMap.clear();

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

        // Build tile-to-gate lookup map
        for (Gate gate : gates) {
            for (Vector2 tile : gate.getTiles()) {
                long key = TileUtils.positionToKey(tile);
                tileToGateMap.put(key, gate);
            }
        }

        gates.removeIf(gate -> gate.getTiles().isEmpty());
    }

    private void fillGates(
        List<Gate> outGates,
        Vector2 neighborGateOffset,
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
                Vector2 outTile = new Vector2(inTile).add(neighborGateOffset);

                Cluster neigbor = clustersManager.getClusterByTilePosition(outTile);
                boolean inTileWalkable = sg.findUnitAt(inTile.x, inTile.y) == null;
                boolean outTileWalkable = sg.findUnitAt(outTile.x, outTile.y) == null;

                if (neigbor != null && outTileWalkable && inTileWalkable) {
                    if (tempGate == null) {
                        tempGate = new Gate(new ArrayList<>(), neigbor, neighborGateOffset);
                        outGates.add(tempGate);
                    }
                    tempGate.addTile(inTile);
                } else {
                    tempGate = null;
                }
            }
        }
    }

    public void calculateGatesLinks() {
        // Clear existing links
        for (Gate gate : gates) {
            gate.clearReachableGates();
        }

        // Calculate paths between all pairs of gates within this cluster
        for (Gate gateA : gates) {
            for (Gate gateB : gates) {
                if (gateA != gateB) {
                    // Skip if we already calculated this path (from B to A)
                    if (gateA.getReachableGates().containsKey(gateB)) {
                        continue;
                    }

                    PathfindingResult result = AStarPathfinder.findPath(
                        this,
                        gateA.getMiddlePoint(),
                        gateB.getMiddlePoint()
                    );

                    if (result.isSuccess()) {
                        // Use the actual path cost from the pathfinding result
                        float pathCost = result.getPathCost();

                        // Add bidirectional links since path cost is the same in both directions
                        gateA.addReachableGate(gateB, pathCost);
                        gateB.addReachableGate(gateA, pathCost);
                    }
                }
            }
        }
    }

    private boolean isWithinCluster(Vector2 tilePos) {
        float clusterWorldSize = tileSize * clusterCellsSize;
        float minX = clusterPosition.x * clusterWorldSize;
        float minY = clusterPosition.y * clusterWorldSize;
        float maxX = minX + clusterWorldSize;
        float maxY = minY + clusterWorldSize;

        return tilePos.x > minX && tilePos.x < maxX && tilePos.y > minY && tilePos.y < maxY;
    }

    @Override
    public List<Vector2> getNeighbors(Vector2 node) {
        List<Vector2> neighbors = new ArrayList<>();
        UnitsSpatialHashGrid sg = clustersManager.getUnitsSpatialHashGrid();

        // Check 4 adjacent tiles: up, down, left, right
        Vector2[] directions = {
            new Vector2(0, tileSize),      // up
            new Vector2(0, -tileSize),     // down
            new Vector2(-tileSize, 0),     // left
            new Vector2(tileSize, 0)       // right
        };

        for (Vector2 dir : directions) {
            Vector2 neighbor = new Vector2(node).add(dir);

            // Check if within cluster bounds and walkable
            if (isWithinCluster(neighbor) && sg.findUnitAt(neighbor.x, neighbor.y) == null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    @Override
    public float getCost(Vector2 from, Vector2 to) {
        return from.dst(to);
    }

    @Override
    public float getHeuristic(Vector2 from, Vector2 to) {
        return from.dst(to);
    }

    public Gate getGateByPosition(Vector2 position) {
        long key = TileUtils.positionToKey(position);
        return tileToGateMap.get(key);
    }

    public List<Gate> getGates() {
        return gates;
    }

    public void debug() {
        float clusterWorldSize = tileSize * clusterCellsSize * 0.95f;
        DebugDrawComponent.getInstance().drawRectangle(getClusterCenter(),
            clusterWorldSize, new Color(1f, 1f, 0f, 0.1f), 0.1f);

//        gates.forEach(gateA -> {
//            gates.forEach(gateB -> {
//                if (gateA != gateB) {
//                    List<Vector2> path = AStarPathfinder.findPath(
//                        this,
//                        gateA.getMiddlePoint(),
//                        gateB.getMiddlePoint()
//                    );
//                    if (path != null && !path.isEmpty()) {
//                        for (int i = 1; i < path.size(); i++) {
//                            debugDrawComponent.drawLine(
//                                path.get(i - 1),
//                                path.get(i),
//                                Color.BROWN,
//                                0.1f
//                            );
//                        }
//                    } else {
//                        debugDrawComponent.drawText(gateA.getMiddlePoint(), "oops", Color.BLACK, 0.1f);
//                        debugDrawComponent.drawLine(
//                            gateA.getMiddlePoint(),
//                            gateB.getMiddlePoint(),
//                            new Color(1f, 1f, 0f, 0.2f),
//                            0.1f
//                        );
//                    }
//                }
//            });
//        });

        gates.forEach(gate -> gate.debug());

    }
}
