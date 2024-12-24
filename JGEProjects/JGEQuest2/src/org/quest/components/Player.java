package org.quest.components;

import java.io.File;

public class Player extends org.jge.demo.Player {

    private boolean dead = false;

    @Override
    public void init() throws Exception {
        super.init();

        if(scene().isInDesign()) {
            return;
        }

        getCollider().collisionListener = (n, t) -> {
            if(n.name.equals("scene1_lava")) {
                dead = true;
            }
        };
    }

    @Override
    public File loadFile() {
        if(dead) {
            return scene().file;
        }
        return null;
    }
}
