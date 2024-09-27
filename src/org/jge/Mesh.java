package org.jge;

import java.io.File;
import java.util.Vector;

public class Mesh implements Renderable {

    public static class MeshPart {

        public Texture texture = null;
        public Texture decal = null;
        public final Vector<VertexPTN> vertices = new Vector<>();
        public final Vector<Integer> indices = new Vector<>();
        public final AABB bounds = new AABB();

        public void calcBounds() {
            bounds.clear();
            for(VertexPTN v : vertices) {
                bounds.add(v.position);
            }
        }
    }

    public final Vector<MeshPart> parts = new Vector<>();

    private File file;
    private final AABB bounds = new AABB();

    public Mesh(File file) {
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
        int count = 0;

        for(MeshPart part : parts) {
            count += part.indices.size() / 3;
        }
        return count;
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        int count = 0;

        i *= 3;
        for(MeshPart part : parts) {
            int j = i - count;
            count += part.indices.size();
            if(i < count) {
                i = j;
                triangle.p1.set(part.vertices.get(part.indices.get(i + 0)).position);
                triangle.p2.set(part.vertices.get(part.indices.get(i + 1)).position);
                triangle.p3.set(part.vertices.get(part.indices.get(i + 2)).position);
                return triangle.calcPlane();
            }
        }
        return triangle;
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
    }

    @Override
    public int renderShadowPass(Scene scene, Node node, Node light) throws Exception {
        ShadowRenderer renderer = Game.getInstance().getRenderer(ShadowRenderer.class);

        for(MeshPart part : parts) {
            renderer.begin(node, light);
            renderer.push(part.vertices, part.indices, part.indices.size());
            renderer.end();
        }
        return getTriangleCount();
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        LightRenderer renderer = Game.getInstance().getRenderer(LightRenderer.class);

        for(MeshPart part : parts) {
            renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, part.texture, part.decal, node.receivesShadow, node.ambientColor, node.diffuseColor);
            renderer.push(part.vertices, part.indices, part.indices.size());
            renderer.end();
        }
        return getTriangleCount();
    }

    public void calcBounds() {
        bounds.clear();
        for(MeshPart part : parts) {
            bounds.add(part.bounds);
        }
    }

    @Override
    public Renderable newInstance() throws Exception {
        return this;
    }
}
