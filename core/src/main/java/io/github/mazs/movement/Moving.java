package io.github.mazs.movement;

import com.badlogic.gdx.math.Vector2;

public interface Moving {

    void moveTo(float x, float y);
    void patrol(Vector2 to);
}
