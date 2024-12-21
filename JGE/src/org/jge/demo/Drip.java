package org.jge.demo;

import java.io.File;
import java.util.Vector;

import org.jge.AABB;
import org.jge.Game;
import org.jge.IO;
import org.jge.LineRenderer;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Renderable;
import org.jge.Scene;
import org.jge.Sound;
import org.jge.Triangle;

public class Drip extends NodeComponent {

    private class UI implements Renderable {

        private final AABB bounds = new AABB();

        @Override
        public File getFile() {
            return null;
        }

        @Override
        public AABB getBounds() {
            bounds.clear();
            bounds.add(-radius, -radius, -radius);
            
            return bounds.add(radius, radius, radius);
        }

        @Override
        public int getTriangleCount() {
            return 2;
        }

        @Override
        public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
            int d = 16;
            if(i == 0) {
                triangle.p1.set(-d, 0, -d);
                triangle.p2.set(-d, 0, +d);
                triangle.p3.set(+d, 0, +d);
            } else {
                triangle.p1.set(+d, 0, +d);
                triangle.p2.set(+d, 0, -d);
                triangle.p3.set(-d, 0, -d);
            }
            return triangle.calcPlane();
        }

        @Override
        public void update(Scene scene, Node node) throws Exception {
        }

        @Override
        public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
            LineRenderer renderer = Game.getInstance().getRenderer(LineRenderer.class);

            renderer.begin(scene.projection, scene.view, node.model);
            renderer.push(-8, 0,  0, 1, 1, 1, 1, 8,  0, 0, 1, 1, 1, 1);
            renderer.push(0, 0,  -8, 1, 1, 1, 1, 0,  0, 8, 1, 1, 1, 1);
            for(int i = 0; i != 128; i++) {
                float r1 = (float)Math.PI * 2 * (i / 127.0f);
                float r2 = (float)Math.PI * 2 * (((i + 1) % 128) / 127.0f);
                float x1 = (float)Math.sin(r1) * radius;
                float z1 = (float)Math.cos(r1) * radius;
                float x2 = (float)Math.sin(r2) * radius;
                float z2 = (float)Math.cos(r2) * radius;

                renderer.push(x1, 0, z1, 1, 1, 1, 1, x2, 0, z2, 1, 1, 1, 1);
            }
            renderer.end();

            return 0;
        }

        @Override
        public Renderable newInstance() throws Exception {
            return new UI();
        }
        
    }

    public float radius = 300;

    private Sound sound;
    private Node player = null;

    @Override
    public void init() throws Exception {
        if(scene().isInDesign()) {
            node().renderable = new UI();
        } else {
            sound = Game.getInstance().getAssets().load(IO.file("assets/amb.wav"));
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
    }

    @Override
    public void update() throws Exception {
        if(player != null) {
            sound.setVolume(1 - Math.min(player.absolutePosition.distance(node().absolutePosition) / radius, 1));
        }
    }
}