package org.game.demos;

import java.io.File;
import java.util.Vector;

import org.game.BlendState;
import org.game.CullState;
import org.game.DepthState;
import org.game.GFX;
import org.game.Game;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.ThreadHelper;
import org.game.UI;
import org.game.UIColors;
import org.game.kits.dun.DunKit;
import org.game.kits.pf.PFKit;

public class Main {

    public static abstract class Kit {

        public abstract void run() throws Exception;

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static void run(Vector<Object> kits) throws Exception {
        while(true) {
            Game game = null;
            boolean quit = true;
            int kit = -1;

            try {
                game = new Game(300, 350, "Kits", false);

                game.getResources().setRoot(new File("kits"));

                SpriteRenderer spriteRenderer = game.getResources().manage(new SpriteRenderer());
                Texture font = game.getResources().load(new File("sprites.png"));
                UI ui = new UI(spriteRenderer, font, 16, 10, 12, 14, 5);
                int sel = 0;
                int iKit = -1;

                while(game.beginFrame()) {
                    GFX.clear(UIColors.BACKGROUND);
                    GFX.setDepthState(DepthState.NONE);
                    GFX.setBlendState(BlendState.ALPHA);
                    GFX.setCullState(CullState.NONE);
                    ui.begin();
                    ui.moveTo(10, 10);
                    iKit = ui.list("kit.list", kits, 10, 25, sel, 0);
                    ui.addRow(5);
                    if(ui.button("edit.button", "Edit", false, 0)) {
                        kit = iKit;
                    }
                    if(ui.button("quit.button", "Quit", false, 5)) {
                        break;
                    }
                    ui.end(game);
                    game.endFrame();

                    sel = -2;

                    if(kit != -1) {
                        quit = false;
                        break;
                    }
                }
            } finally {
                game.destroy();
            }
            if(quit) {
                break;
            }

            if(kit != -1) {
                ((Kit)kits.get(kit)).run();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ThreadHelper.startOnFirstThread(args, "JGE.jar");

        Vector<Object> kits = new Vector<>();

        kits.add(new DunKit());
        kits.add(new PFKit());

        run(kits);
    }
}