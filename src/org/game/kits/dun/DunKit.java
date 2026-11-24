package org.game.kits.dun;

import java.io.File;

import org.game.Game;
import org.game.GameSystem;
import org.game.GameSystem2;
import org.game.KeyFrameMesh;
import org.game.demos.Main.Kit;

public class DunKit extends Kit {

    @Override
    public void run() throws Exception {
        Game game = null;

        try {
            game = new Game(900, 600, "JGE", true);

            game.getResources().setRoot(new File("kits", "dunKit"));

            game.getResources().setLoader((file, extension) -> {
                if(file.isDirectory()) {
                    return KeyFrameMesh.load(file);
                }
                return null;
            });

            GameSystem gs = new GameSystem2(
                "scene1.scene", true, true,
                new Torch(4, 2, 0, 1),
                new Model("coins.obj", false),
                new Model("corner.obj", true),
                new Model("cornerTall.obj", true),
                new Model("door.obj", false),
                new Model("doorWayTall.obj", true),
                new Model("flat.obj", true),
                new Model("flatTall.obj", true),
                new Model("hall.obj", true),
                new Model("hallDoor.obj", true),
                new Model("hallRamp.obj", true), 
                new Model("roomDoor.obj", true), 
                new Model("side.obj", true),
                new Model("sideTall.obj", true),
                new Axis(),
                new Player(),
                new KFModel("Monster")
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
