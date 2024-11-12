package org.jge;

import java.io.File;
import java.util.Vector;

import org.joml.Vector4f;

public class Mesh implements Renderable {

    public static class MeshPart {

        public Texture texture = null;
        public Texture decal = null;
        public final Vector<VertexPT2N> vertices = new Vector<>();
        public final Vector<Integer> indices = new Vector<>();
        public final Vector<int[]> polygons = new Vector<>();
        public final AABB bounds = new AABB();

        private final VertexPT2N vertex = new VertexPT2N();

        public void calcBounds() {
            bounds.clear();
            for(VertexPT2N v : vertices) {
                bounds.add(v.position);
            }
        }

        public void addVertex(float x, float y, float z, float s, float t, float nx, float ny, float nz) throws Exception {
            vertex.position.set(x, y, z);
            vertex.textureCoordinate.set(s, t);
            vertex.textureCoordinate2.set(0, 0);
            vertex.normal.set(nx, ny, nz);
            if(vertex.normal.length() > 0.0000001) {
                vertex.normal.normalize();
            }
            vertices.add(vertex.newInstance());
        }

        public void addPolygon(int ... indices) {
            int tris = indices.length - 2;

            for(int i = 0; i != tris; i++) {
                this.indices.add(indices[0]);
                this.indices.add(indices[i + 1]);
                this.indices.add(indices[i + 2]);
            }
            polygons.add(indices.clone());
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
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        if(!node.lightMapEnabled) {
            LightRenderer renderer = Game.getInstance().getRenderer(LightRenderer.class);

            for(MeshPart part : parts) {
                renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, part.texture, part.decal, node.receivesShadow, node.ambientColor, node.diffuseColor);
                renderer.push(part.vertices, part.indices, part.indices.size());
                renderer.end();
            }
        } else {
            DualTextureRenderer renderer = Game.getInstance().getRenderer(DualTextureRenderer.class);
            Texture texture2 = null;
            Vector4f color = scene.lightMapColor;

            if(node.overrideSceneLightMapColor) {
                color = node.lightMapColor;
            }
            
            File file = IO.file(scene.file.getParentFile(), IO.getFilenameWithoutExtension(scene.file) + ".png");

            if(file.exists()) {
                texture2 = Game.getInstance().getAssets().load(file);
            }
            for(MeshPart part : parts) {
                renderer.begin(scene.projection, scene.view, node.model, part.texture, texture2, part.decal, color);
                renderer.push(part.vertices, part.indices, part.indices.size());
                renderer.end();
            }
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
