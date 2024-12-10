package org.jge.demo;

import org.jge.GFX;
import org.jge.Game;
import org.jge.IO;
import org.jge.Scene;
import org.jge.SpriteRenderer;
import org.jge.Texture;
import org.jge.demo.scenes.Scene1;
import org.jge.demo.scenes.Scene2;
import org.jge.demo.scenes.Scene3;
import org.jge.demo.scenes.Scene4;
import org.jge.demo.scenes.Scene5;
import org.lwjgl.glfw.GLFW;

public class App {

    public static abstract class SceneFactory {

        public abstract Scene createScene() throws Exception;

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static void run(int width, int height, boolean resizable, SceneFactory ... factories) throws Exception {
        Game game = null;
        Scene scene = null;
        int i = 0;
        boolean spaceDown = false;
        boolean sDown = false;
        boolean down = false;

        try {
            game = new Game(width, height, resizable);

            while(game.nextFrame()) {
                if(scene == null) {
                    SpriteRenderer renderer = game.getRenderer(SpriteRenderer.class);
                    Texture font = game.assets.load(IO.file("assets/font.png"));
                    int scale = game.getScale();
                    int x = 10 * scale;
                    int y = 10 * scale;
                    int j = 0;

                    GFX.clear(0.2f, 0.2f, 0.2f, 1);
                    renderer.begin();
                    renderer.beginSprite(font);
                    for(SceneFactory factory : factories) {
                        String name = factory.toString();

                        if(i == j) {
                            renderer.push(name, scale, 8, 12, 100, 5, x, y, 1, 0.5f, 0, 1);
                        } else {
                            renderer.push(name, scale, 8, 12, 100, 5, x, y, 1, 1, 1, 1);
                        }
                        y += 12 * scale + 5 * scale;
                        j++;
                    }
                    renderer.endSprite();
                    renderer.end();
                    game.swapBuffers();

                    if(game.isKeyDown(GLFW.GLFW_KEY_ENTER)) {
                        game.assets.clear();
                        scene = factories[i].createScene();
                    }
                    if(game.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                        if(!down) {
                            i = (i + 1) % factories.length;
                            down = true;
                        }
                    } else {
                        down = false;
                    }
                } else {
                    game.sceneRenderer.render(scene);
                    game.swapBuffers();

                    if(game.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
                        if(!spaceDown) {
                            game.toggleFullscreen();
                            spaceDown = true;
                        }
                    } else {
                        spaceDown = false;
                    }

                    if(game.isKeyDown(GLFW.GLFW_KEY_S)) {
                        if(!sDown) {
                            game.toggleSync();
                            sDown = true;
                        }
                    } else {
                        sDown = false;
                    }

                    if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                        game.setContinuousMouseEnabled(false);
                        scene = null;
                        game.assets.clear();
                    }
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {

        run(1000, 550, true, 
            new Scene1(),
            new Scene2(),
            new Scene3(),
            new Scene4(),
            new Scene5()
        );
    }
}
