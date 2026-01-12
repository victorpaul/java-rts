package io.github.mazs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.mazs.components.StatsComponent;
import io.github.mazs.controllers.RtsController;
import io.github.mazs.units.Pawn;
import io.github.mazs.units.Tree;
import io.github.mazs.units.Unit;
import io.github.mazs.worlds.WorldRts;

public class GameScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Viewport viewport;

    private static final float CAMERA_ZOOM = 0.5f;
    private static final float WORLD_WIDTH = 853f * CAMERA_ZOOM;  // 1280 / 1.5 for 1.5x zoom
    private static final float WORLD_HEIGHT = 480f * CAMERA_ZOOM;  // 720 / 1.5 for 1.5x zoom

    private WorldRts world;

    private RtsController rtsController;
    private StatsComponent stats;

    public GameScreen(Main game) {
        this.game = game;
    }

    /**
     * Create tree border and structured obstacle patterns (T, L, I shapes).
     */
    private void createObstaclePatterns(WorldRts world, float centerX, float centerY) {
        final int TILE = WorldRts.TILE_SIZE;
        final int MAP_WIDTH_TILES = 40;
        final int MAP_HEIGHT_TILES = 23;

        // Tree border around the map
        // Bottom border
        for (int x = 0; x < MAP_WIDTH_TILES; x++) {
            world.addUnit(new Tree(world, x * TILE + TILE / 2f, TILE / 2f));
        }
        // Top border
        for (int x = 0; x < MAP_WIDTH_TILES; x++) {
            world.addUnit(new Tree(world, x * TILE + TILE / 2f, (MAP_HEIGHT_TILES - 1) * TILE + TILE / 2f));
        }
        // Left border (excluding corners already placed)
        for (int y = 1; y < MAP_HEIGHT_TILES - 1; y++) {
            world.addUnit(new Tree(world, TILE / 2f, y * TILE + TILE / 2f));
        }
        // Right border (excluding corners already placed)
        for (int y = 1; y < MAP_HEIGHT_TILES - 1; y++) {
            world.addUnit(new Tree(world, (MAP_WIDTH_TILES - 1) * TILE + TILE / 2f, y * TILE + TILE / 2f));
        }

        // T-shaped obstacle (top-left area)
        float tX = centerX - 120;
        float tY = centerY + 40;
        // Horizontal bar of T
        for (int i = 0; i < 7; i++) {
            world.addUnit(new Tree(world, tX + i * TILE, tY));
        }
        // Vertical stem of T
        for (int i = 1; i < 5; i++) {
            world.addUnit(new Tree(world, tX + 3 * TILE, tY - i * TILE));
        }

        // L-shaped obstacle (top-right area)
        float lX = centerX + 80;
        float lY = centerY + 40;
        // Vertical part of L
        for (int i = 0; i < 6; i++) {
            world.addUnit(new Tree(world, lX, lY - i * TILE));
        }
        // Horizontal part of L
        for (int i = 1; i < 5; i++) {
            world.addUnit(new Tree(world, lX + i * TILE, lY - 5 * TILE));
        }

        // I-shaped obstacle (vertical, center-left)
        float iX = centerX - 80;
        float iY = centerY - 60;
        for (int i = 0; i < 8; i++) {
            world.addUnit(new Tree(world, iX, iY + i * TILE));
        }

        // I-shaped obstacle (horizontal, bottom-center)
        float iX2 = centerX - 40;
        float iY2 = centerY - 60;
        for (int i = 0; i < 10; i++) {
            world.addUnit(new Tree(world, iX2 + i * TILE, iY2));
        }

        // T-shaped obstacle (center of map)
        float centerTX = centerX +200;
        float centerTY = centerY+200;
        // Horizontal bar of T
        for (int i = 0; i < 5; i++) {
            world.addUnit(new Tree(world, centerTX - 2 * TILE + i * TILE, centerTY));
        }
        // Vertical stem of T
        for (int i = 1; i < 4; i++) {
            world.addUnit(new Tree(world, centerTX, centerTY - i * TILE));
        }

        // L-shaped obstacle (top-right, near corner)
        float topRightLX = centerX + 340;
        float topRightLY = centerY + 200;
        // Vertical part
        for (int i = 0; i < 4; i++) {
            world.addUnit(new Tree(world, topRightLX, topRightLY - i * TILE));
        }
        // Horizontal part
        for (int i = 1; i < 4; i++) {
            world.addUnit(new Tree(world, topRightLX - i * TILE, topRightLY));
        }

        // Small I-shaped obstacle (top-right area)
        float topRightIX = centerX + 120;
        float topRightIY = centerY + 50;
        for (int i = 0; i < 5; i++) {
            world.addUnit(new Tree(world, topRightIX + i * TILE, topRightIY));
        }
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        world = new WorldRts();

        float unitX = WORLD_WIDTH / 2;
        float unitY = WORLD_HEIGHT / 2;

        // Create test units
        Unit pawn1 = new Pawn(world, unitX + 150, unitY);
        world.addUnit(pawn1);

        // Create obstacle patterns for testing pathfinding
        createObstaclePatterns(world, unitX, unitY);
        world.getClustersManager().generateClusters();
        stats = new StatsComponent();

        rtsController = new RtsController(world, camera, stats);
        rtsController.addSelectedUnit(pawn1);

        Gdx.input.setInputProcessor(rtsController.createInputAdapter());

        Gdx.app.log("GameScreen", "GameScreen initialized");
        Gdx.app.log("GameScreen", "Camera view: " + WORLD_WIDTH + "x" + WORLD_HEIGHT);
        Gdx.app.log("GameScreen", "Unit spawned at: " + unitX + ", " + unitY);
    }

    @Override
    public void render(float delta) {
        stats.beginFrame();

        ScreenUtils.clear(0.2f, 0.4f, 0.2f, 1f);

        stats.beginUpdate();
        rtsController.update(delta);
        world.update(delta);
        stats.endUpdate();

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

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
        viewport.update(width, height, true);
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
