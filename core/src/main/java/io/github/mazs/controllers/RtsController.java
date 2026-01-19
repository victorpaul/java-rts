package io.github.mazs.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.mazs.components.StatsComponent;
import io.github.mazs.effects.LeftClickEffect;
import io.github.mazs.effects.RightClickEffect;
import io.github.mazs.movement.Moving;
import io.github.mazs.units.Unit;
import io.github.mazs.worlds.WorldRts;

import java.util.ArrayList;
import java.util.List;

public class RtsController {
    private static final float CAMERA_SPEED = 300f;
    private static final float CAMERA_ZOOM = 0.5f;
    private static final float VIEWPORT_WIDTH = 853f * CAMERA_ZOOM;
    private static final float VIEWPORT_HEIGHT = 480f * CAMERA_ZOOM;

    private WorldRts world;
    private OrthographicCamera camera;
    private Viewport viewport;
    private StatsComponent stats;

    private List<Unit> selectedUnits = new ArrayList<>();

    private boolean isDragging = false;
    private float dragStartX, dragStartY;
    private float dragCurrentX, dragCurrentY;

    public RtsController(WorldRts world, StatsComponent stats) {
        this.world = world;
        this.stats = stats;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void update(float delta) {
        handleCameraMovement(delta);
    }

    private void handleCameraMovement(float delta) {
        float moveAmount = CAMERA_SPEED * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.y += moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.y -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.position.x -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.position.x += moveAmount;
        }

        float halfWidth = camera.viewportWidth / 2;
        float halfHeight = camera.viewportHeight / 2;
        int worldPixelWidth = world.getWorldWidthTiles() * WorldRts.TILE_SIZE;
        int worldPixelHeight = world.getWorldHeightTiles() * WorldRts.TILE_SIZE;

        camera.position.x = Math.max(halfWidth, Math.min(camera.position.x, worldPixelWidth - halfWidth));
        camera.position.y = Math.max(halfHeight, Math.min(camera.position.y, worldPixelHeight - halfHeight));
    }

    public InputAdapter createInputAdapter() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                }
                if (keycode == Input.Keys.G) {
                    world.debug = !world.debug;
                }
                if (keycode == Input.Keys.F) {
                    stats.setEnabled(!stats.isEnabled());
                }
                return super.keyDown(keycode);
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));

                if (button == Input.Buttons.LEFT) {
                    isDragging = true;
                    dragStartX = worldCoords.x;
                    dragStartY = worldCoords.y;
                    dragCurrentX = worldCoords.x;
                    dragCurrentY = worldCoords.y;

                    return true;
                }

                if (button == Input.Buttons.RIGHT) {
                    world.addEffect(new RightClickEffect(world, worldCoords.x, worldCoords.y));

                    selectedUnits.forEach(u -> {
                        if (u instanceof Moving) {
                            ((Moving) u).moveTo(worldCoords.x, worldCoords.y);
                        }
                    });
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (isDragging) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    dragCurrentX = worldCoords.x;
                    dragCurrentY = worldCoords.y;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT && isDragging) {
                    isDragging = false;

                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    dragCurrentX = worldCoords.x;
                    dragCurrentY = worldCoords.y;

                    float minX = Math.min(dragStartX, dragCurrentX);
                    float minY = Math.min(dragStartY, dragCurrentY);
                    float maxX = Math.max(dragStartX, dragCurrentX);
                    float maxY = Math.max(dragStartY, dragCurrentY);

                    float dragDistance = Math.abs(maxX - minX) + Math.abs(maxY - minY);
                    // single select
                    if (dragDistance < 5f) {
                        Unit clickedUnit = world.findUnitAtPosition(worldCoords.x, worldCoords.y);
                        if (clickedUnit != null) {
                            world.addEffect(new LeftClickEffect(
                                world,
                                clickedUnit.getPosition().x,
                                clickedUnit.getPosition().y));
                            selectedUnits.clear();
                            selectedUnits.add(clickedUnit);
                        }
                    } else {
                        // multiple select
                        selectedUnits.clear();
                        List<Unit> unitsInRect = world.getSpatialGrid().findUnitsInRectangle(minX, minY, maxX, maxY);
                        for (Unit unit : unitsInRect) {
                            if (unit instanceof Moving) {
                                selectedUnits.add(unit);
                                world.addEffect(new LeftClickEffect(
                                    world,
                                    unit.getPosition().x,
                                    unit.getPosition().y));
                            }
                        }
                    }

                    return true;
                }
                return false;
            }
        };
    }

    public void render(SpriteBatch batch) {
        if (isDragging) {
            Texture whitePixel = world.assertsManager.getWhitePixel();

            float minX = Math.min(dragStartX, dragCurrentX);
            float minY = Math.min(dragStartY, dragCurrentY);
            float maxX = Math.max(dragStartX, dragCurrentX);
            float maxY = Math.max(dragStartY, dragCurrentY);
            float width = maxX - minX;
            float height = maxY - minY;

            batch.setColor(0f, 1f, 0f, 0.2f);
            batch.draw(whitePixel, minX, minY, width, height);

            float borderWidth = 2f;
            batch.setColor(0f, 1f, 0f, 0.8f);
            batch.draw(whitePixel, minX, minY, width, borderWidth);
            batch.draw(whitePixel, minX, maxY - borderWidth, width, borderWidth);
            batch.draw(whitePixel, minX, minY, borderWidth, height);
            batch.draw(whitePixel, maxX - borderWidth, minY, borderWidth, height);

            batch.setColor(Color.WHITE);
        }
    }

    public void addSelectedUnit(Unit unit) {
        if (!selectedUnits.contains(unit)) {
            selectedUnits.add(unit);
        }
    }

    public void dispose() {
    }
}
