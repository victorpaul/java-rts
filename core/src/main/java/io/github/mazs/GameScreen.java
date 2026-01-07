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
        Unit pawn1 = new Pawn(world, unitX, unitY);
        Unit pawn2 = new Pawn(world, unitX + 100, unitY + 100);
        world.addUnit(new Tree(world, unitX - 50, unitY - 50));
        world.addUnit(new Tree(world, unitX - 85, unitY - 50));
        world.addUnit(pawn1);
        world.addUnit(pawn2);
        world.addUnit(new Tree(world, unitX + 50, unitY + 50));

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
