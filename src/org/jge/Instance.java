package org.jge;

import java.io.File;
import java.util.Vector;

public class Instance implements Renderable {
    

    public final Vector<VertexPTN> vertices = new Vector<>();
    public final Vector<Integer> indices = new Vector<>();

    private File file;
    private final AABB bounds = new AABB();

    public Instance(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public AABB getBounds() {
        return bounds;
    }

    @Override
    public int getTriangleCount() {
        return indices.size() / 3;
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        i *= 3;
        triangle.p1.set(vertices.get(indices.get(i + 0)).position);
        triangle.p2.set(vertices.get(indices.get(i + 1)).position);
        triangle.p3.set(vertices.get(indices.get(i + 2)).position);
        return triangle.calcPlane();
    }

    @Override
    public void update(Scene scene, Node node) {
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) {
        LightRenderer renderer = Game.getInstance().getLightRenderer();

        renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, node.texture, node.ambientColor, node.diffuseColor);
        renderer.push(vertices, indices, indices.size());
        renderer.end();

        return getTriangleCount();
    }

    public void calcBounds() {
        bounds.clear();
        for(VertexPTN vertex : vertices) {
            bounds.add(vertex.position);
        }
    }

    @Override
    public Renderable newInstance() throws Exception {
        return this;
    }
}
