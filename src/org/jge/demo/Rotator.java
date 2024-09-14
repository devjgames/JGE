package org.jge.demo;

import org.jge.Game;
import org.jge.NodeComponent;

public class Rotator extends NodeComponent {

    public float angularVelocity = 90;

    @Override
    public void update() throws Exception {
        if(!scene().isInDesign()) {
            node().rotation.rotate(Game.getInstance().elapsedTime() * (float)Math.toRadians(angularVelocity), 0, 1, 0);
        }
    }
}
