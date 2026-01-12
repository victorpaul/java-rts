package io.github.mazs.worlds;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mazs.components.AssertsManager;
import io.github.mazs.components.DebugDrawComponent;
import io.github.mazs.components.UnitsSpatialHashGrid;
import io.github.mazs.effects.AnimationEffect;
import io.github.mazs.movement.hpa.ClustersManager;
import io.github.mazs.units.Unit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorldRts {
    private static final int WORLD_WIDTH_TILES = 40;
    private static final int WORLD_HEIGHT_TILES = 23;

    public static final int TILE_SIZE = 16;

    private Texture tilesetTexture;
    private TextureRegion grassTile;

    private List<Unit> units = new ArrayList<>();
    private List<AnimationEffect> effects = new ArrayList<>();

    public boolean debug = true;
    private ClustersManager clustersManager;
    private UnitsSpatialHashGrid spatialGrid;
    public final AssertsManager assertsManager = new AssertsManager();
    private DebugDrawComponent debugDraw;

    public WorldRts() {
        spatialGrid = new UnitsSpatialHashGrid(TILE_SIZE);
        clustersManager = new ClustersManager(
            TILE_SIZE,
            10, spatialGrid,
            new Vector2(
                WORLD_WIDTH_TILES * TILE_SIZE,
                WORLD_HEIGHT_TILES * TILE_SIZE
            )
        );
        debugDraw = new DebugDrawComponent(assertsManager);
        tilesetTexture = new Texture("TinySwords/Terrain/Tileset/Tilemap_color1.png");

        grassTile = new TextureRegion(tilesetTexture, 32, 16, 16, 16);
    }


    public void update(float delta) {
        units.forEach(unit -> {
            unit.update(delta);
            spatialGrid.update(unit);
        });

        effects.forEach(effect -> effect.update(delta));

        Iterator<Unit> unitIterator = units.iterator();
        while (unitIterator.hasNext()) {
            Unit unit = unitIterator.next();
            if (unit.isPendingDestroy()) {
                unit.dispose();
                unitIterator.remove();
            }
        }

        Iterator<AnimationEffect> effectIterator = effects.iterator();
        while (effectIterator.hasNext()) {
            AnimationEffect effect = effectIterator.next();
            if (effect.isFinished()) {
                effect.dispose();
                effectIterator.remove();
            }
        }

        if (debug) {
            clustersManager.debug(debugDraw);
            debugDraw.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        for (int x = 0; x < WORLD_WIDTH_TILES; x++) {
            for (int y = 0; y < WORLD_HEIGHT_TILES; y++) {
                float screenX = x * TILE_SIZE;
                float screenY = y * TILE_SIZE;
                batch.draw(grassTile, screenX, screenY, TILE_SIZE, TILE_SIZE);
            }
        }

        units.stream()
            .sorted((u1, u2) -> Float.compare(u2.getPosition().y, u1.getPosition().y))
            .forEach(unit -> unit.render(batch));
        effects.forEach(effect -> effect.render(batch));

        if (debug) {
            spatialGrid.drawDebug(batch, debugDraw);
            units.forEach(unit -> unit.drawDebug(batch));
            debugDraw.render(batch);
        }


    }

    public int getWorldWidthTiles() {
        return WORLD_WIDTH_TILES;
    }

    public int getWorldHeightTiles() {
        return WORLD_HEIGHT_TILES;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void addEffect(AnimationEffect effect) {
        effects.add(effect);
    }

    public Unit findUnitAtPosition(float x, float y) {
        return spatialGrid.findUnitAt(x, y);
    }

    public UnitsSpatialHashGrid getSpatialGrid() {
        return spatialGrid;
    }

    public ClustersManager getClustersManager() {
        return clustersManager;
    }

    public DebugDrawComponent getDebugDraw() {
        return debugDraw;
    }

    public void dispose() {
        tilesetTexture.dispose();
        units.forEach(Unit::dispose);
        effects.forEach(AnimationEffect::dispose);
        spatialGrid.dispose();
        debugDraw.dispose();
        assertsManager.dispose();
    }
}
