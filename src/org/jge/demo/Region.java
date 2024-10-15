package org.jge.demo;

import java.io.File;
import java.util.Vector;

import org.jge.AABB;
import org.jge.Game;
import org.jge.LineRenderer;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Renderable;
import org.jge.Scene;
import org.jge.Triangle;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Region extends NodeComponent {

    private class Lines implements Renderable {

        private final AABB bounds = new AABB();

        @Override
        public File getFile() {
            return null;
        }

        @Override
        public AABB getBounds() {
            bounds.min.set(-size.x / 2, -10000, -size.y / 2);
            bounds.max.set(+size.x / 2, +10000, +size.y / 2);
            return bounds;
        }

        @Override
        public int getTriangleCount() {
            return 4;
        }

        @Override
        public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
            if(i == 0) {
                triangle.p1.set(-size.x / 2, 0, -size.y / 2);
                triangle.p2.set(-size.x / 2, 0, +size.y / 2);
                triangle.p3.set(+size.x / 2, 0, +size.y / 2);
            } else {
                triangle.p1.set(+size.x / 2, 0, +size.y / 2);
                triangle.p2.set(+size.x / 2, 0, -size.y / 2);
                triangle.p3.set(-size.x / 2, 0, -size.y / 2);
            }
            return triangle.calcPlane();
        }

        @Override
        public void update(Scene scene, Node node) throws Exception {
            bounds.min.set(-size.x / 2, -10000, -size.y / 2);
            bounds.max.set(+size.x / 2, +10000, +size.y / 2);
        }

        @Override
        public int renderShadowPass(Scene scene, Node node, Node light) throws Exception {
            return 0;
        }

        @Override
        public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
            LineRenderer renderer = Game.getInstance().getRenderer(LineRenderer.class);

            renderer.begin(scene.projection, scene.view, node.model);
            renderer.push(bounds.min.x, 0, bounds.min.z, 1, 1, 1, 1, bounds.max.x, 0, bounds.min.z, 1, 1, 1, 1);
            renderer.push(bounds.min.x, 0, bounds.max.z, 1, 1, 1, 1, bounds.max.x, 0, bounds.max.z, 1, 1, 1, 1);
            renderer.push(bounds.min.x, 0, bounds.min.z, 1, 1, 1, 1, bounds.min.x, 0, bounds.max.z, 1, 1, 1, 1);
            renderer.push(bounds.max.x, 0, bounds.min.z, 1, 1, 1, 1, bounds.max.x, 0, bounds.max.z, 1, 1, 1, 1);
            renderer.end();

            return 0;
        }

        @Override
        public Renderable newInstance() throws Exception {
            return this;
        }
        
    }
    
    public final Vector2f size = new Vector2f(64, 64);
    public final Vector3f offset = new Vector3f(1, 100, 0);
    public float speed = 10;

    @Override
    public void init() throws Exception {
        if(scene().isInDesign()) {
            node().renderable = new Lines();
        } 
    }
}
