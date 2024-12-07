package org.jge;

import java.io.File;
import java.util.Vector;

import org.joml.Vector4f;

public class Mesh implements Renderable {

    public final Vector<VertexPT2N> vertices = new Vector<>();
    public final Vector<Integer> indices = new Vector<>();
    public final Vector<int[]> polygons = new Vector<>();
    private final VertexPT2N vertex = new VertexPT2N();

    private final AABB bounds = new AABB();

    @Override
    public File getFile() {
        return null;
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

    @Override
    public void update(Scene scene, Node node) throws Exception {
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        if(!node.lightMapEnabled) {
            LightRenderer renderer = Game.getInstance().getRenderer(LightRenderer.class);

            renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, node.texture, node.decal, node.ambientColor, node.diffuseColor);
            renderer.push(vertices, indices, indices.size());
            renderer.end();
        } else {
            DualTextureRenderer renderer = Game.getInstance().getRenderer(DualTextureRenderer.class);
            Texture texture2 = node.texture2;
            Vector4f color = scene.lightMapColor;

            if(node.overrideSceneLightMapColor) {
                color = node.lightMapColor;
            }
            
            File file = scene.lightMapFile;

            if(file != null && texture2 == null) {
                if(file.exists()) {
                    texture2 = Game.getInstance().assets.load(file);
                }
            }
            renderer.begin(scene.projection, scene.view, node.model, node.texture, texture2, node.decal, color);
            renderer.push(vertices, indices, indices.size());
            renderer.end();
        }
        return getTriangleCount();
    }

    public void calcBounds() {
        bounds.clear();
        for(VertexPT2N v : vertices) {
            bounds.add(v.position);
        }
    }

    @Override
    public Renderable newInstance(boolean deepCopy) throws Exception {
        if(deepCopy) {
            Mesh mesh = new Mesh();

            mesh.vertices.addAll(vertices);
            mesh.indices.addAll(indices);
            mesh.polygons.addAll(polygons);
            mesh.bounds.set(bounds);

            return mesh;
        }
        return this;
    }
}
