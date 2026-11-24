package org.game;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class VertexArrayBuffer extends Resource {
    
    private int vbo;
    private FloatBuffer buf = null;
    private int count = 0;
    private int countAdder;

    public  VertexArrayBuffer(int countAdder) {
        vbo = GL15.glGenBuffers();

        this.countAdder = Math.max(0, countAdder);
    }

    public int getCount() {
        return count;
    }

    public float get(int i) throws Exception {
        if(i < 0 || i >= count) {
            throw new Exception("vertex index out of bounds");
        }
        return buf.get(i);
    }

    int getVBO() {
        return  vbo;
    }

    private void ensureBuffer(int count) {
        if(buf == null) {
            Log.getInstance().put(1, "creating vertex array buffer " + count + " ...");
            buf = BufferUtils.createFloatBuffer(count);
        } else if(buf.position() + count > buf.capacity()) {
            count += buf.capacity() + countAdder;
            Log.getInstance().put(1, "increasing vertex array buffer capacity to " + count + " ...");
            FloatBuffer nbuf = BufferUtils.createFloatBuffer(buf.position() + count);

            buf.flip();
            nbuf.put(buf);
            buf = nbuf;
        }
    }

    public void begin(int count) {
        ensureBuffer(count);

        buf.position(0);
        buf.limit(buf.capacity());

        this.count = 0;
    }

    public void push(float x) {
        ensureBuffer(1);
        buf.put(x);

        count++;
    }

    public void push(float x, float y) {
        ensureBuffer(2);
        buf.put(x);
        buf.put(y);

        count += 2;
    }

    public void push(float x, float y, float z) {
        ensureBuffer(3);
        buf.put(x);
        buf.put(y);
        buf.put(z);

        count += 3;
    }

    public void push(float x, float y, float z, float w) {
        ensureBuffer(4);
        buf.put(x);
        buf.put(y);
        buf.put(z);
        buf.put(w);

        count += 4;
    }

    public void push(Vec2 v) {
        push(v.x, v.y);
    }

    public void push(Vec3 v) {
        push(v.x, v.y, v.z);
    }

    public void push(Vec4 v) {
        push(v.x, v.y, v.z, v.w);
    }

    public void end(BufferUsage usage) {
        if(buf.position() != 0) {
            buf.flip();

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GFX.getUsage(usage));
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        buf.position(0);
        buf.limit(buf.capacity());
    }

    public void calcBounds(AABB b, VertexAttribute ... attributes) throws Exception {
        GFX.validate(null, count, attributes);

        int stride = GFX.stride(attributes);

        b.clear();

        for(int i = 0; i != getCount(); i += stride) {
            float x = get(i + 0);
            float y = get(i + 1);
            float z = get(i + 2);

            b.union(x, y, z);
        }
    }

    @Override
    public void destroy() {
        GL15.glDeleteBuffers(vbo);
        super.destroy();
    }
}
