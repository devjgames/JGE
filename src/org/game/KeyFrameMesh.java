package org.game;

import java.io.File;
import java.util.Vector;

public class KeyFrameMesh extends Resource {

    public static KeyFrameMesh load(File directory) throws Exception {
        File[] list = directory.listFiles();
        Vector<File> files = new Vector<>();

        for(File file : list) {
            if(file.getName().endsWith(".obj")) {
                files.add(file);
            }
        }
        files.sort((a, b) -> a.getName().compareTo(b.getName()));

        Vector<Mesh> frames = new Vector<>();

        for(File file : files) {
            frames.add(Game.getInstance().getResources().load(new File(directory.getName(), file.getName())));
        }
        return new KeyFrameMesh(frames, directory);
    }

    public final File file;

    private Vector<Mesh> frames;
    private Mesh mesh;
    private int frame = 0;
    private int start = 0;
    private int end = 0;
    private int speed = 0;
    private float amount = 0;
    private boolean done = true;
    private boolean looping = false;
    private boolean paused = false;

    public KeyFrameMesh(Vector<Mesh> frames, File file) throws Exception {
        this.frames = frames;
        this.file = file;

        mesh = new Mesh();

        setup();
    }

    public KeyFrameMesh(KeyFrameMesh keyFrameMesh) throws Exception {
        frames = keyFrameMesh.frames;
        file = keyFrameMesh.file;

        mesh = new Mesh();

        setup();
    }

    private void setup() throws Exception {
        for(int i = 0; i != frames.get(0).getPartCount(); i++) {
            MeshPart p = new MeshPart(0, 0, mesh);
            MeshPart p2 = frames.get(0).getPart(i);
            Texture texture = (p2.get(MeshLoader.TEXTURE));

            if(texture != null) {
                p.put(MeshLoader.TEXTURE, texture);
            }

            p.vertexElementBuffer.begin(p2.vertexElementBuffer.getCount());
            for(int j = 0; j != p2.vertexElementBuffer.getCount(); j += 3) {
                int i1 = p2.vertexElementBuffer.get(j + 0);
                int i2 = p2.vertexElementBuffer.get(j + 1);
                int i3 = p2.vertexElementBuffer.get(j + 2);

                p.vertexElementBuffer.push(i1, i2, i3);
            }
            p.vertexElementBuffer.end(BufferUsage.STATIC);

            mesh.addPart(p);
        }
        reset();
    }

    public Mesh getMesh() {
        return mesh;
    }

    public int getCount() {
        return frames.size();
    }

    public Mesh getFrame(int i) {
        return frames.get(i);
    }

    public boolean isDone() {
        return done;
    }

    public void reset() throws Exception {
        frame = start;
        done = start == end;
        amount = 0;
        lerp();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setSequence(int start, int end, int speed, boolean looping) throws Exception {
        if(this.start != start || this.end != end || this.speed != speed || this.looping != looping) {
            if(start >= 0 && start < frames.size() && end >= 0 && end < frames.size() && start <= end && speed >= 0) {
                this.start = start;
                this.end = end;
                this.speed = speed;
                this.looping = looping;

                reset();
            }
        }
    }

    public void update() throws Exception {
        if(paused || done) {
            return;
        }

        amount += speed * Game.getInstance().getElapsedTime();
        if(amount >= 1) {
            if(looping) {
                if(frame == end) {
                    frame = start;
                } else {
                    frame++;
                }
                amount = 0;
            } else if(frame == end - 1) {
                amount = 1;
                done = true;
            } else {
                amount = 0;
                frame++;
            }
        }
        lerp();
    }

    @Override
    public void destroy() {
        mesh.destroy();

        super.destroy();
    }

    private void lerp() throws Exception {
        int f1 = frame;
        int f2 = frame + 1;

        if(frame == end) {
            f2 = start;
        }

        Mesh frame1 = frames.get(f1);
        Mesh frame2 = frames.get(f2);

        for(int i = 0; i != mesh.getPartCount(); i++) {
            MeshPart p = mesh.getPart(i);
            MeshPart p1 = frame1.getPart(i);
            MeshPart p2 = frame2.getPart(i);

            p.vertexBuffer.begin(p1.vertexBuffer.getCount());
            for(int j = 0; j != p1.vertexBuffer.getCount(); j += BasicRenderer.VERTEX_STRIDE) {
                float x1 = p1.vertexBuffer.get(j + 0);
                float y1 = p1.vertexBuffer.get(j + 1);
                float z1 = p1.vertexBuffer.get(j + 2);
                float s = p1.vertexBuffer.get(j + 3);
                float t = p1.vertexBuffer.get(j + 4);
                float nx1 = p1.vertexBuffer.get(j + 5);
                float ny1 = p1.vertexBuffer.get(j + 6);
                float nz1 = p1.vertexBuffer.get(j + 7);
                float x2 = p2.vertexBuffer.get(j + 0);
                float y2 = p2.vertexBuffer.get(j + 1);
                float z2 = p2.vertexBuffer.get(j + 2);
                float nx2 = p2.vertexBuffer.get(j + 5);
                float ny2 = p2.vertexBuffer.get(j + 6);
                float nz2 = p2.vertexBuffer.get(j + 7);
                float x = x1 + amount * (x2 - x1);
                float y = y1 + amount * (y2 - y1);
                float z = z1 + amount * (z2 - z1);
                float nx = nx1 + amount * (nx2 - nx1);
                float ny = ny1 + amount * (ny2 - ny1);
                float nz = nz1 + amount * (nz2 - nz1);

                p.vertexBuffer.push(x, y, z);
                p.vertexBuffer.push(s, t);
                p.vertexBuffer.push(nx, ny, nz);
                p.vertexBuffer.push(1, 1, 1, 1);
            }
            p.vertexBuffer.end(BufferUsage.DYNAMIC);
        }

        for(int i = 0; i != mesh.getPartCount(); i++) {
            MeshPart p = mesh.getPart(i);
            MeshPart p1 = frames.get(f1).getPart(i);
            MeshPart p2 = frames.get(f2).getPart(i);
            AABB b1 = p1.bounds;
            AABB b2 = p2.bounds;
            AABB bounds = p.getIBounds();

            bounds.minX = b1.minX + amount * (b2.minX - b1.minX);
            bounds.minY = b1.minY + amount * (b2.minY - b1.minY);
            bounds.minZ = b1.minZ + amount * (b2.minZ - b1.minZ);

            bounds.maxX = b1.maxX + amount * (b2.maxX - b1.maxX);
            bounds.maxY = b1.maxY + amount * (b2.maxY - b1.maxY);
            bounds.maxZ = b1.maxZ + amount * (b2.maxZ - b1.maxZ);
        }
    }
}
