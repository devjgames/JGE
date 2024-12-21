package org.quest.components;

import org.jge.Game;
import org.jge.NodeComponent;

public class Item extends NodeComponent {

    public float angularVelocity = 90;
    
    @Override
    public void update() throws Exception {
        if(!scene().isInDesign()) {
            node().rotation.rotate((float)Math.toRadians(angularVelocity) * Game.getInstance().elapsedTime(), 0, 1, 0);
        }
    }
}
