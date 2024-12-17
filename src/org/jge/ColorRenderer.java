package org.jge;

import java.nio.FloatBuffer;
import java.util.Vector;

import org.joml.Matrix4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

public class ColorRenderer extends Renderer {

    public static final int VERTEX_STRIDE = 9;
    
    private final Shader shader;
    private final int vbo;
    private FloatBuffer vBuf = Buffers.newDirectFloatBuffer(6 * VERTEX_STRIDE);

    public ColorRenderer() throws Exception {
        shader = new Shader(
            IO.readAllBytes(Shader.class, "/org/jge/glsl/ColorVertexShader.glsl"), 
            IO.readAllBytes(Shader.class, "/org/jge/glsl/ColorFragmentShader.glsl"),
            "aPosition", "aTextureCoordinate", "aColor"
        );

        int[] b = new int[1];

        Game.getGL().glGenBuffers(1, b, 0);
        vbo = b[0];
    }

    public void begin(Matrix4f projection, Matrix4f view, Matrix4f model, Texture texture) {
        GL2 gl = Game.getGL();

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo);
        shader.begin();
        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, VERTEX_STRIDE * 4, 0);
        gl.glVertexAttribPointer(1, 2, GL2.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 3);
        gl.glVertexAttribPointer(2, 4, GL2.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 5);
        shader.set("uProjection", projection);
        shader.set("uView", view);
        shader.set("uModel", model);
        shader.set("uTextureEnabled", texture != null);
        if(texture != null) {
            shader.bind(GL2.GL_TEXTURE_2D, "uTexture", 0, texture.id);
        }
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void push(Vector<VertexPTC> vertices, Vector<Integer> indices, int indexCount) {
        if(vBuf.position() + indices.size() * VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + indices.size() * VERTEX_STRIDE;

            System.out.println("increasing color renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = Buffers.newDirectFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        for(int i = 0; i != indexCount; i++) {
            VertexPTC v = vertices.get(indices.get(i));

            vBuf.put(v.position.x);
            vBuf.put(v.position.y);
            vBuf.put(v.position.z);
            vBuf.put(v.textureCoordinate.x);
            vBuf.put(v.textureCoordinate.y);
            vBuf.put(v.color.x);
            vBuf.put(v.color.y);
            vBuf.put(v.color.z);
            vBuf.put(v.color.w);
        }
    }

    public void push(VertexPTC v) {
        if(vBuf.position() + VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + 600 * VERTEX_STRIDE;

            System.out.println("increasing color renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = Buffers.newDirectFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }

        vBuf.put(v.position.x);
        vBuf.put(v.position.y);
        vBuf.put(v.position.z);
        vBuf.put(v.textureCoordinate.x);
        vBuf.put(v.textureCoordinate.y);
        vBuf.put(v.color.x);
        vBuf.put(v.color.y);
        vBuf.put(v.color.z);
        vBuf.put(v.color.w);
    }

    void end(int primType) {
        GL2 gl = Game.getGL();
        int count = vBuf.position() / VERTEX_STRIDE;

        if(count > 0) {
            vBuf.flip();
            gl.glBufferData(GL2.GL_ARRAY_BUFFER, count * VERTEX_STRIDE * 4, vBuf, GL2.GL_DYNAMIC_DRAW);
            gl.glDrawArrays(primType, 0, count);
        }
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        shader.end();
    }

    public void end() {
        end(GL2.GL_TRIANGLES);
    }

    @Override
    public void destroy() throws Exception {
        shader.destroy();
        Game.getGL().glDeleteBuffers(1, new int[] { vbo }, 0);
        super.destroy();
    }
}
