package io.github.mazs.effects;

import io.github.mazs.worlds.WorldRts;

public class RightClickEffect extends AnimationEffect {

    public RightClickEffect(WorldRts world, float x, float y) {
        super(
            world.assertsManager.getTexture("TinySwords/UI Elements/UI Elements/Cursors/Cursor_02.png"),
            x, y, 1);
    }
}
