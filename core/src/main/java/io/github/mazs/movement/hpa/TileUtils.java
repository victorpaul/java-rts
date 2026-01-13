package io.github.mazs.movement.hpa;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.worlds.WorldRts;

public class TileUtils {

    /**
     * Snaps a position to the nearest tile center.
     * @param position The position to snap
     * @return A new Vector2 at the tile center
     */
    public static Vector2 snapToTileCenter(Vector2 position) {
        int cellX = (int) (position.x / WorldRts.TILE_SIZE);
        int cellY = (int) (position.y / WorldRts.TILE_SIZE);
        return new Vector2(
            cellX * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f,
            cellY * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f
        );
    }

    /**
     * Snaps a position to the nearest tile center (in-place mutation).
     * @param position The position to snap (modified in place)
     */
    public static void snapToTileCenterInPlace(Vector2 position) {
        int cellX = (int) (position.x / WorldRts.TILE_SIZE);
        int cellY = (int) (position.y / WorldRts.TILE_SIZE);
        position.set(
            cellX * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f,
            cellY * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f
        );
    }

    /**
     * Converts a world position to a unique grid cell key.
     * @param position The world position
     * @return A unique long key representing the grid cell
     */
    public static long positionToKey(Vector2 position) {
        int cellX = (int) (position.x / WorldRts.TILE_SIZE);
        int cellY = (int) (position.y / WorldRts.TILE_SIZE);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    /**
     * Converts world coordinates to a unique grid cell key.
     * @param x The world x coordinate
     * @param y The world y coordinate
     * @return A unique long key representing the grid cell
     */
    public static long positionToKey(float x, float y) {
        int cellX = (int) (x / WorldRts.TILE_SIZE);
        int cellY = (int) (y / WorldRts.TILE_SIZE);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }
}
