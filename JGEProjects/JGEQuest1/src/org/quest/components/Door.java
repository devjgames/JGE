package org.quest.components;

import org.jge.AABB;
import org.jge.Node;
import org.jge.NodeComponent;
import org.joml.Vector2f;

public class Door extends NodeComponent {

    private Node player = null;
    private float height = 0;

    @Override
    public void start() throws Exception {
        if(scene().isInDesign()) {
            return;
        }
        scene().root.traverse((n) -> {
            for(int i = 0; i != n.getComponentCount(); i++) {
                NodeComponent component = n.getComponent(i);

                if(component instanceof Player) {
                    player = n;
                }
            }
            return player == null;
        });

        AABB b = node().bounds;
        
        height = b.max.y - b.min.y + 50;
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        float d = Vector2f.distance(
            node().absolutePosition.x, node().absolutePosition.z, 
            player.position.x, player.position.z
            );
        
        node().position.y = -(1 - Math.min(1, d / 100)) * height;
    }
}