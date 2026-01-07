package io.github.mazs.effects;

import io.github.mazs.worlds.WorldRts;

public class LeftClickEffect extends AnimationEffect {

    public LeftClickEffect(WorldRts world, float x, float y) {
        super(
            world.assertsManager.getTexture("TinySwords/UI Elements/UI Elements/Cursors/Cursor_04.png"),
            x, y, 1);
    }
}
