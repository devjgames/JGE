package org.jge.demo.components;

import org.jge.NodeComponent;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Door extends NodeComponent {

    private float height;
    private final Vector3f position = new Vector3f();
    
    @Override
    public void init() throws Exception {
        height = node().bounds.max.y - node().bounds.min.y + 50;
        node().bounds.center(position);
        position.y = node().bounds.min.y;
    }

    @Override
    public void update() throws Exception {
        float d = Vector2f.distance(position.x, position.z, scene().eye.x, scene().eye.z);

        node().position.y = position.y - (1 - Math.min(d / height, 1)) * height;
    }
}
