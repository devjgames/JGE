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

            game.getResources().setLoader((file, extension) -> {
                if(file.isDirectory()) {
                    return KeyFrameMesh.load(file);
                }
                return null;
            });

            game.getResources().setRoot(new File("kits", "dunKit"));

            GameSystem gs = new GameSystem2(
                "scene1.scene", true, true,
                new Torch(4, 2, 0, 1),
                new Model("models", "coins.obj"),
                new Model("models", "corner.obj"),
                new Model("models", "door.obj"),
                new Model("models", "flats.obj"),
                new Model("models", "hall.obj"),
                new Model("models", "hallDoor.obj"),
                new Model("models", "hallEnd.obj"),
                new Model("models", "hallEndT.obj"), 
                new Model("models", "hallRamp.obj"), 
                new Model("models", "hallShort.obj"),
                new Model("models", "hallDoor.obj"),
                new Model("models", "roomDoor1.obj"),
                new Model("models", "roomDoor2.obj"),
                new Model("models", "bridge.obj"),
                new Model("models", "lava.obj"),
                new Model("models", "roomLava1.obj"),
                new Model("models", "roomLava2.obj"),
                new Model("models", "orb.obj"),
                new Model("models", "key.obj"),
                new Model("models", "heal.obj"),
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
