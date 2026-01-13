package io.github.mazs.movement.hpa;

import com.badlogic.gdx.math.Vector2;

import java.util.*;

import static io.github.mazs.movement.hpa.TileUtils.positionToKey;

public class AStarPathfinder {

    private static class Node {
        Vector2 position;
        Node parent;
        float gCost;  // Cost from start
        float hCost;  // Heuristic to goal
        float fCost;  // gCost + hCost

        Node(Vector2 position, Node parent, float gCost, float hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

    public static PathfindingResult findPath(PathfindingGraph graph, Vector2 start, Vector2 end) {
        // Snap start and end positions to tile centers for grid-based pathfinding
        start = TileUtils.snapToTileCenter(start);
        end = TileUtils.snapToTileCenter(end);
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<Long, Node> openMap = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        int nodesChecked = 0;

        Node startNode = new Node(start, null, 0, graph.getHeuristic(start, end));
        openSet.add(startNode);
        openMap.put(positionToKey(start), startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            openMap.remove(positionToKey(current.position));
            nodesChecked++;

            // Goal reached
            if (graph.isGoalReached(current.position, end)) {
                List<Vector2> path = reconstructPath(current);
                return new PathfindingResult(path, nodesChecked, openSet.size(), current.gCost);
            }

            graph.addToClosedSet(closedSet,current.position);

            // Check all neighbors
            for (Vector2 neighborPos : graph.getNeighbors(current.position)) {
                long neighborKey = positionToKey(neighborPos);

                if (closedSet.contains(neighborKey)) {
                    continue;
                }

                float newGCost = current.gCost + graph.getCost(current.position, neighborPos);

                Node existingNode = openMap.get(neighborKey);
                if (existingNode == null) {
                    // New node
                    float hCost = graph.getHeuristic(neighborPos, end);
                    Node neighborNode = new Node(neighborPos, current, newGCost, hCost);
                    openSet.add(neighborNode);
                    openMap.put(neighborKey, neighborNode);
                } else if (newGCost < existingNode.gCost) {
                    // Better path found
                    openSet.remove(existingNode);
                    existingNode.gCost = newGCost;
                    existingNode.fCost = newGCost + existingNode.hCost;
                    existingNode.parent = current;
                    openSet.add(existingNode);
                }
            }
        }

        return PathfindingResult.failure(nodesChecked, openSet.size());
    }

    private static List<Vector2> reconstructPath(Node endNode) {
        List<Vector2> path = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }
}
