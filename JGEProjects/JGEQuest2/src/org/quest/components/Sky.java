package org.quest.components;

import org.jge.DepthState;
import org.jge.NodeComponent;

public class Sky extends NodeComponent {

    @Override
    public void start() throws Exception {
        if(!scene().isInDesign()) {
            node().position.set(scene().eye);
        }

        node().depthState = DepthState.NONE;
        node().zOrder = -1000;
    }

    @Override
    public void update() throws Exception {
        if(!scene().isInDesign()) {
            node().position.set(scene().eye);
        }
    }
}