package io.github.mazs.movement.hpa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.DebugDrawComponent;

import java.util.Set;

public class Gate {
    private final Set<Vector2> tiles;
    private final Cluster neighborCluster;
    private final Vector2 direction;

    public Gate(Set<Vector2> tiles, Cluster cluster, Vector2 direction) {
        this.tiles = tiles;
        this.neighborCluster = cluster;
        this.direction = direction;
    }

    void addTile(Vector2 tile) {
        tiles.add(tile);
    }

    void debug(DebugDrawComponent debugDrawComponent) {

        tiles.forEach(tile -> {
            debugDrawComponent.drawLine(tile, neighborCluster.getClusterCenter(), Color.BLACK, 0.1f);
        });

    }
}
