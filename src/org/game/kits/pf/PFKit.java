package org.game.kits.pf;

import java.io.File;

import org.game.Game;
import org.game.GameSystem;
import org.game.GameSystem2;
import org.game.demos.Main.Kit;

public class PFKit extends Kit {
    
    @Override
    public void run() throws Exception {
        Game game = null;

        try {
            game = new Game(900, 600, "JGE", true);

            game.getResources().setRoot(new File("kits", "pfKit"));

            GameSystem gs = new GameSystem2(
                "scene1.scene", true, true,
                new Model("gem.obj"),
                new Model("grass-corner.obj"),
                new Model("grass.obj"),
                new Model("rock.obj"),
                new Model("sky.obj"),
                new Model("stone.obj"),
                new Axis(),
                new Player(),
                new Torch(1, 1, 1, 1)

            );

            game.resetTimer();

            while(game.beginFrame()) {
                gs.update();
                game.endFrame();
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
    
}
