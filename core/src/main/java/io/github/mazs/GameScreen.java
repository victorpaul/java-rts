package io.github.mazs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.mazs.components.StatsComponent;
import io.github.mazs.controllers.RtsController;
import io.github.mazs.movement.hpa.Cluster;
import io.github.mazs.movement.hpa.Gate;
import io.github.mazs.units.Pawn;
import io.github.mazs.units.Tree;
import io.github.mazs.units.Unit;
import io.github.mazs.worlds.WorldRts;

import java.util.List;
import java.util.Random;

public class GameScreen implements Screen {
    private final Main game;
    private OrthographicCamera uiCamera;

    private WorldRts world;

    private RtsController rtsController;
    private StatsComponent stats;

    public GameScreen(Main game) {
        this.game = game;
    }

    private static final long OBSTACLE_SEED = 12345L;

    /**
     * Create tree border and random obstacle patterns (T, L, I shapes) in each cluster.
     */
    private void createObstaclePatterns(WorldRts world) {
        final int TILE = WorldRts.TILE_SIZE;
        final int mapWidthTiles = world.getWorldWidthTiles();
        final int mapHeightTiles = world.getWorldHeightTiles();

        // Tree border around the map
        createBorder(world, TILE, mapWidthTiles, mapHeightTiles);

        // Generate random obstacles in each cluster
        Random random = new Random(OBSTACLE_SEED);
        int clusterSize = 10; // tiles per cluster
        int clustersX = (int) Math.ceil((double) mapWidthTiles / clusterSize);
        int clustersY = (int) Math.ceil((double) mapHeightTiles / clusterSize);

        for (int cx = 0; cx < clustersX; cx++) {
            for (int cy = 0; cy < clustersY; cy++) {
                int obstacleCount = 1 + random.nextInt(5); // 1-3 obstacles per cluster
                for (int o = 0; o < obstacleCount; o++) {
                    createRandomObstacle(world, random, cx, cy, clusterSize, TILE, clustersX, clustersY);
                }
            }
        }
    }

    private void createBorder(WorldRts world, int TILE, int mapWidthTiles, int mapHeightTiles) {
        // Bottom border
        for (int x = 0; x < mapWidthTiles; x++) {
            world.addUnit(new Tree(world, x * TILE + TILE / 2f, TILE / 2f));
        }
        // Top border
        for (int x = 0; x < mapWidthTiles; x++) {
            world.addUnit(new Tree(world, x * TILE + TILE / 2f, (mapHeightTiles - 1) * TILE + TILE / 2f));
        }
        // Left border (excluding corners)
        for (int y = 1; y < mapHeightTiles - 1; y++) {
            world.addUnit(new Tree(world, TILE / 2f, y * TILE + TILE / 2f));
        }
        // Right border (excluding corners)
        for (int y = 1; y < mapHeightTiles - 1; y++) {
            world.addUnit(new Tree(world, (mapWidthTiles - 1) * TILE + TILE / 2f, y * TILE + TILE / 2f));
        }
    }

    private void createRandomObstacle(WorldRts world, Random random, int clusterX, int clusterY,
                                       int clusterSize, int TILE, int clustersX, int clustersY) {
        // Calculate cluster bounds in world coordinates
        float clusterStartX = clusterX * clusterSize * TILE;
        float clusterStartY = clusterY * clusterSize * TILE;

        // Adjust margins for edge clusters (extra margin near world borders)
        int marginLeft = (clusterX == 0) ? 3 : 2;
        int marginRight = (clusterX == clustersX - 1) ? 3 : 2;
        int marginBottom = (clusterY == 0) ? 3 : 2;
        int marginTop = (clusterY == clustersY - 1) ? 3 : 2;

        int availableX = clusterSize - marginLeft - marginRight;
        int availableY = clusterSize - marginBottom - marginTop;

        // Skip if not enough space
        if (availableX < 2 || availableY < 2) {
            return;
        }

        float x = clusterStartX + (marginLeft + random.nextInt(availableX)) * TILE + TILE / 2f;
        float y = clusterStartY + (marginBottom + random.nextInt(availableY)) * TILE + TILE / 2f;

        int shapeType = random.nextInt(3); // 0=T, 1=L, 2=I
        int size = 2 + random.nextInt(3); // 2-4 tiles

        switch (shapeType) {
            case 0: createTShape(world, x, y, size, TILE, random.nextBoolean()); break;
            case 1: createLShape(world, x, y, size, TILE, random.nextInt(4)); break;
            case 2: createIShape(world, x, y, size, TILE, random.nextBoolean()); break;
        }
    }

    private void createTShape(WorldRts world, float x, float y, int size, int TILE, boolean vertical) {
        if (vertical) {
            // Vertical T
            for (int i = 0; i < size; i++) {
                world.addUnit(new Tree(world, x, y + i * TILE));
            }
            for (int i = 1; i <= size / 2; i++) {
                world.addUnit(new Tree(world, x - i * TILE, y + (size / 2) * TILE));
                world.addUnit(new Tree(world, x + i * TILE, y + (size / 2) * TILE));
            }
        } else {
            // Horizontal T
            for (int i = 0; i < size; i++) {
                world.addUnit(new Tree(world, x + i * TILE, y));
            }
            for (int i = 1; i <= size / 2; i++) {
                world.addUnit(new Tree(world, x + (size / 2) * TILE, y - i * TILE));
                world.addUnit(new Tree(world, x + (size / 2) * TILE, y + i * TILE));
            }
        }
    }

    private void createLShape(WorldRts world, float x, float y, int size, int TILE, int rotation) {
        // rotation: 0=normal, 1=90deg, 2=180deg, 3=270deg
        int dx1 = 0, dy1 = 1, dx2 = 1, dy2 = 0;
        switch (rotation) {
            case 1: dx1 = -1; dy1 = 0; dx2 = 0; dy2 = 1; break;
            case 2: dx1 = 0; dy1 = -1; dx2 = -1; dy2 = 0; break;
            case 3: dx1 = 1; dy1 = 0; dx2 = 0; dy2 = -1; break;
        }

        for (int i = 0; i < size; i++) {
            world.addUnit(new Tree(world, x + dx1 * i * TILE, y + dy1 * i * TILE));
        }
        for (int i = 1; i < size; i++) {
            world.addUnit(new Tree(world, x + dx2 * i * TILE, y + dy2 * i * TILE));
        }
    }

    private void createIShape(WorldRts world, float x, float y, int size, int TILE, boolean vertical) {
        for (int i = 0; i < size; i++) {
            if (vertical) {
                world.addUnit(new Tree(world, x, y + i * TILE));
            } else {
                world.addUnit(new Tree(world, x + i * TILE, y));
            }
        }
    }

    @Override
    public void show() {
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        world = new WorldRts();

        // Create obstacle patterns for testing pathfinding
        createObstaclePatterns(world);

        // Generate clusters after obstacles are placed
        world.getClustersManager().generateClusters();

        // Spawn unit at first gate of cluster (1,1) to avoid border trees
        Vector2 spawnPosition = getSpawnPositionFromCluster(0, 0);
        Unit pawn1 = new Pawn(world, spawnPosition.x, spawnPosition.y);
        world.addUnit(pawn1);

        stats = new StatsComponent();
        rtsController = new RtsController(world, stats);
        rtsController.addSelectedUnit(pawn1);

        Gdx.input.setInputProcessor(rtsController.createInputAdapter());
    }

    private Vector2 getSpawnPositionFromCluster(int clusterX, int clusterY) {
        Cluster cluster = world.getClustersManager().getCluster(clusterX, clusterY);
        if (cluster != null) {
            List<Gate> gates = cluster.getGates();
            if (!gates.isEmpty()) {
                return gates.get(0).getMiddlePoint();
            }
        }
        // Fallback to cluster center if no gates
        int clusterWorldSize = WorldRts.TILE_SIZE * 10; // clusterCellsSize = 10
        return new Vector2(
            clusterX * clusterWorldSize + clusterWorldSize / 2f,
            clusterY * clusterWorldSize + clusterWorldSize / 2f
        );
    }

    @Override
    public void render(float delta) {
        stats.beginFrame();

        ScreenUtils.clear(0.2f, 0.4f, 0.2f, 1f);

        stats.beginUpdate();
        rtsController.update(delta);
        world.update(delta);
        stats.endUpdate();

        rtsController.getCamera().update();
        game.batch.setProjectionMatrix(rtsController.getCamera().combined);

        stats.beginRender();
        game.batch.begin();
        world.render(game.batch);
        rtsController.render(game.batch);
        game.batch.end();
        stats.endRender();

        // Render stats in screen coordinates
        uiCamera.update();
        game.batch.setProjectionMatrix(uiCamera.combined);
        game.batch.begin();
        stats.render(game.batch, 10, Gdx.graphics.getHeight() - 10);
        game.batch.end();

        stats.endFrame();
    }

    @Override
    public void resize(int width, int height) {
        rtsController.getViewport().update(width, height, true);
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        world.dispose();
        rtsController.dispose();
        stats.dispose();
    }
}
