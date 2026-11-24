package org.game;

import java.io.File;

public abstract class GameComponent {

    public GameComponent() {
    }

    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        return null;
    }
    
    public abstract void init(GameSystem gs, GameNode node) throws Exception;

    public void update(GameSystem gs, GameNode node) throws Exception {
    }

    public abstract boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception;

    public abstract void render(GameSystem gs, GameNode node, BasicRenderer basicRenderer, RenderPass renderPass) throws Exception;

    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
    }

    public void parseValue(GameSystem gs, GameNode node) throws Exception {
    }

    public File loadFile(GameSystem gs, GameNode node) throws Exception {
        return null;
    }

    public void finish() throws Exception {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().toLowerCase();
    }
}
