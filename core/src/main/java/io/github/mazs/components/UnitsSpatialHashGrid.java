package io.github.mazs.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.movement.Moving;
import io.github.mazs.units.Unit;

import java.util.*;
import java.util.stream.Collectors;

public class UnitsSpatialHashGrid {
    private final int cellSize;
    private final Map<Long, List<Unit>> grid = new HashMap<>();
    private final Map<Unit, Long> unitToCellKey = new HashMap<>();

    public UnitsSpatialHashGrid(int cellSize) {
        this.cellSize = cellSize;
    }

    private long getCellKey(float x, float y) {
        int cellX = (int) (x / cellSize);
        int cellY = (int) (y / cellSize);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    private int getCellX(long key) {
        return (int) (key >> 32);
    }

    private int getCellY(long key) {
        return (int) key;
    }

    public void update(Unit unit) {
        Vector2 pos = unit.getPosition();
        long newKey = getCellKey(pos.x, pos.y);
        Long oldKey = unitToCellKey.get(unit);

        if (oldKey == null) {
            grid.computeIfAbsent(newKey, k -> new ArrayList<>()).add(unit);
            unitToCellKey.put(unit, newKey);
        } else if (oldKey != newKey) {
            List<Unit> oldCell = grid.get(oldKey);
            if (oldCell != null) {
                oldCell.remove(unit);
                if (oldCell.isEmpty()) {
                    grid.remove(oldKey);
                }
            }

            grid.computeIfAbsent(newKey, k -> new ArrayList<>()).add(unit);
            unitToCellKey.put(unit, newKey);
        }
        // If oldKey == newKey, unit is still in same cell - no update needed
    }

    public void remove(Unit unit) {
        Long key = unitToCellKey.remove(unit);
        if (key != null) {
            List<Unit> cell = grid.get(key);
            if (cell != null) {
                cell.remove(unit);
                if (cell.isEmpty()) {
                    grid.remove(key);
                }
            }
        }
    }

    public Unit findUnitAt(Vector2 tile) {
        long key = getCellKey(tile.x, tile.y);
        List<Unit> cell = grid.get(key);

        if (cell != null && !cell.isEmpty()) {
            return cell.get(0);
        }
        return null;
    }

    public boolean isBlockedByStaticUnit(Vector2 position) {
        return Optional.ofNullable(grid.get(getCellKey(position.x, position.y)))
            .map(units -> !units.stream().allMatch(unit -> unit instanceof Moving))
            .orElse(false);
    }

    public boolean isBlocked(Vector2 position) {
        return Optional.ofNullable(grid.get(getCellKey(position.x, position.y)))
            .map(units -> !units.isEmpty())
            .orElse(false);
    }

    public boolean isBlockedByDynamic(Vector2 position) {
        return Optional.ofNullable(grid.get(getCellKey(position.x, position.y)))
            .map(units -> units.stream().allMatch(unit -> unit instanceof Moving))
            .orElse(false);
    }

    public List<Unit> findUnitsInRectangle(float minX, float minY, float maxX, float maxY) {
        List<Unit> unitsInRect = new ArrayList<>();

        int minCellX = (int) (minX / cellSize);
        int minCellY = (int) (minY / cellSize);
        int maxCellX = (int) (maxX / cellSize);
        int maxCellY = (int) (maxY / cellSize);

        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellY = minCellY; cellY <= maxCellY; cellY++) {
                long key = ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
                List<Unit> cell = grid.get(key);

                if (cell != null) {
                    // Check each unit in the cell to see if it's actually within the rectangle
                    for (Unit unit : cell) {
                        Vector2 pos = unit.getPosition();
                        if (pos.x >= minX && pos.x <= maxX && pos.y >= minY && pos.y <= maxY) {
                            if (!unitsInRect.contains(unit)) {
                                unitsInRect.add(unit);
                            }
                        }
                    }
                }
            }
        }

        return unitsInRect;
    }

    public void snapToGrid(Vector2 position) {
        int cellX = (int) (position.x / cellSize);
        int cellY = (int) (position.y / cellSize);

        position.set(
            cellX * cellSize + cellSize / 2f,
            cellY * cellSize + cellSize / 2f
        );
    }

    public void drawDebug(SpriteBatch batch, DebugDrawComponent debugDraw) {
        for (Map.Entry<Long, List<Unit>> entry : grid.entrySet()) {
            long key = entry.getKey();
            int cellX = getCellX(key);
            int cellY = getCellY(key);
            int unitCount = entry.getValue().size();

            Color color;
            if (unitCount == 1) {
                color = new Color(0f, 1f, 0f, 0.3f);
            } else if (unitCount <= 3) {
                color = new Color(1f, 1f, 0f, 0.4f);
            } else {
                color = new Color(1f, 0f, 0f, 0.5f);
            }

            float worldX = cellX * cellSize;
            float worldY = cellY * cellSize;
            debugDraw.drawRectangleImmediate(batch, worldX, worldY, cellSize, cellSize, color);
        }
    }

    public void dispose() {
    }
}
