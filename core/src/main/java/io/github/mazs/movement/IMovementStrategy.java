package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.mazs.units.Unit;

/**
 * Interface for different movement/pathfinding strategies.
 * Allows switching between different approaches (steering behaviors, Warcraft 2 style, etc.)
 */
public interface IMovementStrategy {

    void resetState();

    Vector2 calculateNextCell(Unit owner, Vector2 targetFinalPosition);

}
