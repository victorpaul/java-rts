package io.github.mazs.components;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.worlds.WorldRts;

public class TileUtils {
    private static final float ARRIVAL_THRESHOLD = 2f;

    public static boolean hasReachedTile(Vector2 position, Vector2 targetTile) {
        return position.epsilonEquals(targetTile, ARRIVAL_THRESHOLD);
    }

    public static Vector2 snapToTileCenter(Vector2 position) {
        int cellX = (int) (position.x / WorldRts.TILE_SIZE);
        int cellY = (int) (position.y / WorldRts.TILE_SIZE);
        return new Vector2(
            cellX * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f,
            cellY * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f
        );
    }

    public static void snapToTileCenterInPlace(Vector2 position) {
        int cellX = (int) (position.x / WorldRts.TILE_SIZE);
        int cellY = (int) (position.y / WorldRts.TILE_SIZE);
        position.set(
            cellX * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f,
            cellY * WorldRts.TILE_SIZE + WorldRts.TILE_SIZE / 2f
        );
    }

    public static long positionToKey(Vector2 position) {
        return positionToKey(position.x, position.y);
    }

    public static long positionToKey(float x, float y) {
        int cellX = (int) (x / WorldRts.TILE_SIZE);
        int cellY = (int) (y / WorldRts.TILE_SIZE);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }
}
