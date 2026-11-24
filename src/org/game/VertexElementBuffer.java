package org.game;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class VertexElementBuffer extends Resource {
    
    private int veo;
    private IntBuffer buf = null;
    private int count = 0;
    private int countAdder;
    private int vertexBufferCount = -1;

    public VertexElementBuffer(int countAdder) {
        veo = GL15.glGenBuffers();

        this.countAdder = Math.max(0, countAdder);
    }

    public int getCount() {
        return count;
    }

    public int get(int i) throws Exception {
        if(i < 0 || i >= count) {
            throw new Exception("vertex element index out of bounds");
        }
        return buf.get(i);
    }

    int getVEO() {
        return  veo;
    }

    private void ensureBuffer(int count) {
        if(buf == null) {
            Log.getInstance().put(1, "creating vertex element buffer " + count + " ...");
            buf = BufferUtils.createIntBuffer(count);
        } else if(buf.position() + count > buf.capacity()) {
            count += buf.capacity() + countAdder;
            Log.getInstance().put(1, "increasing vertex element buffer capacity to " + count + " ...");
            IntBuffer nbuf = BufferUtils.createIntBuffer(buf.position() + count);

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
        vertexBufferCount = -1;
    }

    public void push(int i, int j) {
        ensureBuffer(2);
        buf.put(i);
        buf.put(j);
        
        count += 2;
    }

    public void push(int i, int j, int k) {
        ensureBuffer(3);
        buf.put(i);
        buf.put(j);
        buf.put(k);

        count += 3;
    }

    public void push(int ... indices) {
        int tris = indices.length - 2;

        for(int i = 0; i != tris; i++) {
            push(indices[0], indices[i + 1], indices[i + 2]);
        }
    }

    public void end(BufferUsage usage) {
        if(buf.position() != 0) {
            buf.flip();

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buf, GFX.getUsage(usage));
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        buf.position(0);
        buf.limit(buf.capacity());
    }

    public void validate(VertexArrayBuffer vertexBuffer) throws Exception {
        if(vertexBufferCount != vertexBuffer.getCount()) {
            Log.getInstance().put(1, "validating vertex element buffer ...");
            vertexBufferCount = vertexBuffer.getCount();
            for(int i = 0; i != getCount(); i++) {
                int idx = get(i);

                if(idx < 0 || idx >= vertexBuffer.getCount()) {
                    throw new Exception("vertex index out of bounds");
                }
            }
        }
    }

    @Override
    public void destroy() {
        GL15.glDeleteBuffers(veo);
        super.destroy();
    }
}
