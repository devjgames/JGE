package org.jge;

import java.io.File;
import java.util.Vector;

import org.jge.Mesh.MeshPart;

public class KeyFrameMesh implements Renderable {

    public static class KeyFrameMeshLoader implements AssetLoader {

        @Override
        public Object load(File file, AssetManager assets) throws Exception {
            String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
            Vector<Mesh> frames = new Vector<>();
            Texture texture = null;
            File directory = IO.file(file.getParentFile(), IO.getFilenameWithoutExtension(file));

            for(String line : lines) {
                String tLine = line.trim();

                if(tLine.startsWith("texture ")) {
                    texture = assets.load(IO.file(file.getParentFile(), tLine.substring(7).trim()));
                }
            }

            File[] files = directory.listFiles();
            Vector<File> fList = new Vector<>();

            for(File ifile : files) {
                if(IO.getExtension(ifile).equals(".obj")) {
                    fList.add(ifile);
                }
            }
            fList.sort((a, b) -> a.compareTo(b));

            for(File ifile : fList) {
                frames.add(assets.load(ifile));
            }
            
            KeyFrameMesh mesh = new KeyFrameMesh(file, frames);

            mesh.texture = texture;

            return mesh;
        }
        
    }

    public Texture texture = null;

    private final File file;
    private final AABB bounds = new AABB();
    private final Vector<Mesh> frames = new Vector<>();
    private int frame = 0;
    private float amount = 0;
    private boolean done = true;
    private final Triangle t1 = new Triangle();
    private final Triangle t2 = new Triangle();
    private final VertexPT2N vertex = new VertexPT2N();

    public KeyFrameMesh(File file, Vector<Mesh> frames) {
        this.file = file;
        this.frames.addAll(frames);

        reset();

        bounds.set(frames.get(0).getBounds());
    }

    public KeyFrameMesh(KeyFrameMesh mesh) {
        this.file = mesh.file;
        this.frames.addAll(mesh.frames);
        this.texture = mesh.texture;

        reset();

        bounds.set(frames.get(0).getBounds());
    }

    public boolean isDone() {
        return done;
    }

    public void reset() {
        frame = 0;
        amount = 0;
        done = frames.size() == 1;
        bounds.set(frames.get(0).getBounds());
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
        return frames.get(0).getTriangleCount();
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        int f1 = frame;
        int f2 = frame + 1;

        if(f1 == frames.size() - 1) {
            f2 = f1;
        }

        frames.get(f1).getTriangle(scene, node, i, t1);
        frames.get(f2).getTriangle(scene, node, i, t2); 

        t1.p1.lerp(t2.p1, amount, triangle.p1);
        t1.p2.lerp(t2.p2, amount, triangle.p2);
        t1.p3.lerp(t2.p3, amount, triangle.p3);

        return triangle.calcPlane();
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
        if(done) {
            if(node.looping) {
                reset();
            }
            return;
        }

        amount += node.speed * Game.getInstance().elapsedTime();
        if(amount >= 1) {
            if(node.looping) {
                if(frame == frames.size() - 1) {
                    frame = 0;
                } else {
                    frame++;
                }
                amount = 0;
            } else if(frame == frames.size() - 2) {
                amount = 1;
                done = true;
            } else {
                frame++;
                amount = 0;
            }
        }

        int f1 = frame;
        int f2 = frame + 1;

        if(f1 == frames.size() - 1) {
            f2 = f1;
        }

        AABB b1 = frames.get(f1).getBounds();
        AABB b2 = frames.get(f2).getBounds();

        b1.min.lerp(b2.min, amount, bounds.min);
        b1.max.lerp(b2.max, amount, bounds.max);
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        LightRenderer renderer = Game.getInstance().getRenderer(LightRenderer.class);
        int f1 = frame;
        int f2 = frame + 1;

        if(f1 == frames.size() - 1) {
            f2 = f1;
        }

        Mesh m1 = frames.get(f1);
        Mesh m2 = frames.get(f2);

        renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, texture, null, node.ambientColor, node.diffuseColor);

        for(int i = 0; i != m1.parts.size(); i++) {
            MeshPart p1 = m1.parts.get(i);
            MeshPart p2 = m2.parts.get(i);

            for(int j = 0; j != p1.indices.size(); j++) {
                int i1 = p1.indices.get(j);
                int i2 = p2.indices.get(j);
                VertexPT2N v1 = p1.vertices.get(i1);
                VertexPT2N v2 = p2.vertices.get(i2);

                v1.position.lerp(v2.position, amount, vertex.position);
                v1.normal.lerp(v2.normal, amount, vertex.normal);
                vertex.textureCoordinate.set(v1.textureCoordinate);

                renderer.push(vertex);
            }
        }
        renderer.end();

        return getTriangleCount();
    }

    @Override
    public Renderable newInstance() throws Exception {
        return new KeyFrameMesh(this);
    }
    
}
