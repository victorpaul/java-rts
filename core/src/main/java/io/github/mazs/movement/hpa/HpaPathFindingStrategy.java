package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;
import io.github.mazs.movement.IMovementStrategy;
import io.github.mazs.units.Unit;

import java.util.List;

import static io.github.mazs.components.TileUtils.hasReachedTile;

public class HpaPathFindingStrategy implements IMovementStrategy {

    private final ClustersManager clustersManager;
    private final UnitsSpatialHashGrid unitsSpatialHashGrid;
    private List<Vector2> cachedGlobalPath;
//    private List<Vector2> cachedLocalPath;

    public HpaPathFindingStrategy(ClustersManager clustersManager, UnitsSpatialHashGrid unitsSpatialHashGrid) {
        this.clustersManager = clustersManager;
        this.unitsSpatialHashGrid = unitsSpatialHashGrid;
    }

    @Override
    public void resetState() {
        cachedGlobalPath = null;
//        cachedLocalPath = null;
    }

    @Override
    public Vector2 calculateNextCell(Unit owner, Vector2 targetFinalPosition) {

        Vector2 position = owner.getPosition();
        Cluster currentCluster = clustersManager.getClusterByTilePosition(position);
        Cluster toCluster = clustersManager.getClusterByTilePosition(targetFinalPosition);

        if (currentCluster == toCluster) {

            PathfindingResult localPath = AStarPathfinder.findPath(
                currentCluster,
                position,
                targetFinalPosition);
            if (localPath.isSuccess()) {
                DebugDrawComponent.getInstance()
                    .drawPath(localPath.getPath(), Color.BLUE, 1f);
                return localPath.getPath().get(1);
//                cachedLocalPath = localPath.getPath();
            }
//            Vector2 nextTile = cachedLocalPath.get(0);
//            cachedLocalPath.remove(0);
//            return nextTile;
        }

        List<Vector2> globalPath = getGlobalPath(position, targetFinalPosition);

        if (globalPath != null) {

            // we reached global node, clear local path
            if (atGlobalNode(globalPath, position)) {
                globalPath.remove(0);
//                cachedLocalPath = null;
                // if we are at the gate, return tile of next gate in neighbor cluster
                Gate gate = currentCluster.getGateByPosition(position);
                if (gate != null) {
                    return gate.getNeighborGate(clustersManager).getMiddlePoint();
                }
            }

            PathfindingResult localPath = AStarPathfinder.findPath(
                currentCluster,
                owner.getPosition(),
                globalPath.isEmpty() ? targetFinalPosition : globalPath.get(0));
            if (localPath.isSuccess()) {
                DebugDrawComponent.getInstance()
                    .drawPath(localPath.getPath(), Color.RED, 1f);
                return localPath.getPath().get(1);
            }

        }
        return null;


//        ok, here use AStarPathfinder to feed cluster manger to find global path
//        and then use current cluster to calculate local path from nodes to nodes in realtime
//
//        keep path in memory unless you faced obstacle, BTW, global path finding, should ignore units
//
//        Also,if unit died, or destroyed, we may need to mark cluster as dirty, to recalculate links between gates


    }

    private boolean atGlobalNode(List<Vector2> globalPath, Vector2 position) {
        return globalPath != null &&
            !globalPath.isEmpty() &&
            hasReachedTile(globalPath.get(0), position);
    }

    private List<Vector2> getLocalPath(Vector2 from, Vector2 to) {
        return null;
    }

    private List<Vector2> getGlobalPath(Vector2 from, Vector2 to) {
        if (cachedGlobalPath == null) {
            PathfindingResult globalPath = AStarPathfinder.findPath(
                clustersManager,
                from,
                to);

            if (globalPath.isSuccess()) {
                cachedGlobalPath = globalPath.getPath();
                DebugDrawComponent.getInstance()
                    .drawPath(cachedGlobalPath, Color.BLUE, 3f);
            }
        }
        return cachedGlobalPath;
    }
}
