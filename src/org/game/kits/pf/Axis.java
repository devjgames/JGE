package org.game.kits.pf;

import org.game.GameNode;
import org.game.GameSystem;

public class Axis extends Model {
    
    public Axis() {
        super("axis.obj");
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign()) {
            node.position.set(gs.target);
        }
    }

    @Override
    public String toString() {
        return "axis";
    }
}

