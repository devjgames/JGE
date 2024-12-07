package org.jge.demo.components;

import org.jge.Game;
import org.jge.IO;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Sound;

public class Drip extends NodeComponent {

    public float radius = 300;

    private Sound sound;
    private Node player = null;

    @Override
    public void init() throws Exception {
        sound = Game.getInstance().assets.load(IO.file("assets/amb.wav"));
        sound.setVolume(0);
        sound.play(true);

        scene().root.traverse((n) -> {
            for(int i = 0; i != n.getComponentCount(); i++) {
                NodeComponent component = n.getComponent(i);

                if(component instanceof Player) {
                    player = n;
                }
            }
            return true;
        });
    }

    @Override
    public void update() throws Exception {
        if(player != null) {
            sound.setVolume(1 - Math.min(player.absolutePosition.distance(node().absolutePosition) / radius, 1));
        }
    }
}