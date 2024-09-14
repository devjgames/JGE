package org.jge.demo;

import org.jge.DepthState;
import org.jge.NodeComponent;

public class Sky extends NodeComponent {
    

    @Override
    public void start() throws Exception {
        if(!scene().isInDesign()) {
            node().position.set(scene().eye);
            node().getChild(0).depthState = DepthState.NONE;
        }
    }

    @Override
    public void update() throws Exception {
        if(!scene().isInDesign()) {
            node().position.set(scene().eye);
        }
    }
}
