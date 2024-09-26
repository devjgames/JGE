package org.jge;

import java.io.*;
import java.util.Vector;

public class MD2Mesh implements Renderable {

    public static class MD2Header {
        public final int id;
        public final int version;
        public final int skinW;
        public final int skinH;
        public final int frameSize;
        public final int numSkins;
        public final int numXYZ;
        public final int numST;
        public final int numTris;
        public final int numGLCmds;
        public final int numFrames;
        public final int offSkins;
        public final int offST;
        public final int offTris;
        public final int offFrames;
        public final int offGLCmds;
        public final int offEnd;

        public MD2Header(BinReader reader) {
            id = reader.readInt();
            version = reader.readInt();
            skinW = reader.readInt();
            skinH = reader.readInt();
            frameSize = reader.readInt();
            numSkins = reader.readInt();
            numXYZ = reader.readInt();
            numST = reader.readInt();
            numTris = reader.readInt();
            numGLCmds = reader.readInt();
            numFrames = reader.readInt();
            offSkins = reader.readInt();
            offST = reader.readInt();
            offTris = reader.readInt();
            offFrames = reader.readInt();
            offGLCmds = reader.readInt();
            offEnd = reader.readInt();
        }
    }

    public static class MD2TextureCoordinate {
        public final int s;
        public final int t;

        public MD2TextureCoordinate(BinReader reader) {
            s = reader.readShort();
            t = reader.readShort();
        }
    }

    public static class MD2Triangle {
        public final int[] xyz;
        public final int[] st;

        public MD2Triangle(BinReader reader) {
            xyz = new int[3];
            st = new int[3];
            xyz[0] = reader.readShort();
            xyz[1] = reader.readShort();
            xyz[2] = reader.readShort();
            st[0] = reader.readShort();
            st[1] = reader.readShort();
            st[2] = reader.readShort();
        }
    }

    public static class MD2Vertex {
        public final int[] xyz;
        public final int n;

        public MD2Vertex(BinReader reader) {
            xyz = new int[3];
            xyz[0] = reader.readByte();
            xyz[1] = reader.readByte();
            xyz[2] = reader.readByte();
            n = reader.readByte();
        }
    }

    public static class MD2Frame {
        public final float[] scale;
        public final float[] translation;
        public final String name;
        public final MD2Vertex[] vertices;
        public final AABB bounds;

        public MD2Frame(BinReader reader, MD2Header header) {
            scale = new float[3];
            translation = new float[3];
            scale[0] = reader.readFloat();
            scale[1] = reader.readFloat();
            scale[2] = reader.readFloat();
            translation[0] = reader.readFloat();
            translation[1] = reader.readFloat();
            translation[2] = reader.readFloat();
            name = reader.readString(16);
            vertices = new MD2Vertex[header.numXYZ];
            bounds = new AABB();
            for(int j = 0; j != header.numXYZ; j++) {
                vertices[j] = new MD2Vertex(reader);
                float x = vertices[j].xyz[0] * scale[0] + translation[0];
                float y = vertices[j].xyz[1] * scale[1] + translation[1];
                float z = vertices[j].xyz[2] * scale[2] + translation[2];
                bounds.add(x, y, z);
            }
        }
    }

    public final File file;

    private MD2Header header;
    private MD2TextureCoordinate[] textureCoordinates;
    private MD2Triangle[] triangles;
    private MD2Frame[] frames;
    private boolean done;
    private int frame;
    private int start;
    private int end;
    private int speed;
    private boolean looping;
    private float amount;
    private float[][] normals;
    protected final Vector<VertexPTN> vertices = new Vector<>();
    protected final Vector<Integer> indices = new Vector<>();
    private final AABB bounds = new AABB();

    public MD2Mesh(File file) throws Exception {
        BinReader reader = new BinReader(IO.readAllBytes(file));

        this.file = file;

        header = new MD2Header(reader);
        reader.setPosition(header.offST);
        textureCoordinates = new MD2TextureCoordinate[header.numST];
        for(int j = 0; j != header.numST; j++) {
            textureCoordinates[j] = new MD2TextureCoordinate(reader);
        }
        triangles = new MD2Triangle[header.numTris];
        reader.setPosition(header.offTris);
        for(int j = 0; j != header.numTris; j++) {
            triangles[j] = new MD2Triangle(reader);
        }
        frames = new MD2Frame[header.numFrames];
        for(int j = 0; j != header.numFrames; j++) {
            reader.setPosition(header.offFrames + j * header.frameSize);
            frames[j] = new MD2Frame(reader, header);
        }

        for(int i = 0, j = 0; i != header.numTris; i++) {
            vertices.add(new VertexPTN());
            vertices.add(new VertexPTN());
            vertices.add(new VertexPTN());
            indices.add(j++);
            indices.add(j++);
            indices.add(j++);
        }

        normals = MD2Normals.cloneNormals();

        start = end = speed = 0;
        looping = false;

        reset();
    }

    public MD2Mesh(MD2Mesh mesh) throws Exception {
        header = mesh.header;
        textureCoordinates = mesh.textureCoordinates;
        triangles = mesh.triangles;
        frames = mesh.frames;
        normals = mesh.normals;

        for(int i = 0, j = 0; i != header.numTris; i++) {
            vertices.add(new VertexPTN());
            vertices.add(new VertexPTN());
            vertices.add(new VertexPTN());
            indices.add(j++);
            indices.add(j++);
            indices.add(j++);
        }

        start = end = speed = 0;
        looping = false;

        reset();

        this.file = mesh.file;

        setSequence(mesh.start, mesh.end, mesh.speed, mesh.looping);
    }

    public File getFile() {
        return file;
    }

    public AABB getBounds() {
        return bounds;
    }

    public int getTriangleCount() {
        return header.numTris;
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        i *= 3;
        triangle.p1.set(vertices.get(i + 0).position);
        triangle.p2.set(vertices.get(i + 1).position);
        triangle.p3.set(vertices.get(i + 2).position);

        return triangle.calcPlane();
    }

    public boolean isDone() {
        return done;
    }

    public void reset() {
        frame = start;
        amount = 0;
        done = start == end;
        bounds.set(frames[frame].bounds);
        buffer();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setSequence(int start, int end, int speed, boolean looping) {
        if(start != this.start || end != this.end || speed != this.speed || looping != this.looping) {
            if(start >= 0 && start < header.numFrames && end >= 0 && end < header.numFrames && speed >= 0 && start <= end) {
                this.start = start;
                this.end = end;
                this.speed = speed;
                this.looping = looping;
                reset();
            }
        }
    }

    public void setFrame(int frame) {
        if(frame >= start && frame <= end) {
            this.frame = frame;
            bounds.set(frames[frame].bounds);
            buffer();
        }
    }

    public void buffer() {
        int f1 = frame;
        int f2 = f1 + 1;

        if(f1 == end) {
            f2 = start;
        }

        for(int i = 0, k = 0; i != header.numTris; i++) {
            for(int j = 2; j != -1; j--, k++) {
                MD2TextureCoordinate textureCoordinate = textureCoordinates[triangles[i].st[j]];
                float s = textureCoordinate.s / (float)header.skinW;
                float t = textureCoordinate.t / (float)header.skinH;
                float vx1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[0] * frames[f1].scale[0] + frames[f1].translation[0];
                float vy1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[1] * frames[f1].scale[1] + frames[f1].translation[1];
                float vz1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[2] * frames[f1].scale[2] + frames[f1].translation[2];
                float vx2 = frames[f2].vertices[triangles[i].xyz[j]].xyz[0] * frames[f2].scale[0] + frames[f2].translation[0];
                float vy2 = frames[f2].vertices[triangles[i].xyz[j]].xyz[1] * frames[f2].scale[1] + frames[f2].translation[1];
                float vz2 = frames[f2].vertices[triangles[i].xyz[j]].xyz[2] * frames[f2].scale[2] + frames[f2].translation[2];
                float nx1 = -normals[frames[f1].vertices[triangles[i].xyz[j]].n][0];
                float ny1 = -normals[frames[f1].vertices[triangles[i].xyz[j]].n][1];
                float nz1 = -normals[frames[f1].vertices[triangles[i].xyz[j]].n][2];
                float nx2 = -normals[frames[f2].vertices[triangles[i].xyz[j]].n][0];
                float ny2 = -normals[frames[f2].vertices[triangles[i].xyz[j]].n][1];
                float nz2 = -normals[frames[f2].vertices[triangles[i].xyz[j]].n][2];
                VertexPTN v = vertices.get(k);

                v.position.set(vx1 + amount * (vx2 - vx1), vy1 + amount * (vy2 - vy1), vz1 + amount * (vz2 - vz1));
                v.textureCoordinate.set(s, t);
                v.normal.set(nx1 + amount * (nx2 - nx1), ny1 + amount * (ny2 - ny1), nz1 + amount * (nz2 - nz1));
            }
        }
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        LightRenderer renderer = Game.getInstance().getRenderer(LightRenderer.class);

        renderer.begin(scene.projection, scene.view, node.model, node.modelIT, lights, node.texture, node.ambientColor, node.diffuseColor);
        renderer.push(vertices, indices, indices.size());
        renderer.end();

        return getTriangleCount();
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
        if(done) {
            return;
        }
        amount += speed * Game.getInstance().elapsedTime();
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
                frame++;
                amount = 0;
            }
        }

        int f1 = frame;
        int f2 = f1 + 1;

        if(f1 == end) {
            f2 = start;
        }
        frames[f1].bounds.min.lerp(frames[f2].bounds.min, amount, bounds.min);
        frames[f1].bounds.max.lerp(frames[f2].bounds.max, amount, bounds.max);

        buffer();
    }

    @Override
    public Renderable newInstance() throws Exception {
        return new MD2Mesh(this);
    }
}
