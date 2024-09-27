package org.jge;

import java.nio.FloatBuffer;
import java.util.Vector;

import org.joml.Matrix4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

public class ShadowRenderer extends Renderer {
    
    public static final int VERTEX_STRIDE = 3;
    
    private final Shader shader;
    private final int vbo;
    private FloatBuffer vBuf = Buffers.newDirectFloatBuffer(6 * VERTEX_STRIDE);
    private final Matrix4f matrix = new Matrix4f();

    public ShadowRenderer() throws Exception {
        shader = new Shader(
            IO.readAllBytes(Shader.class, "/org/jge/glsl/ShadowVertexShader.glsl"), 
            IO.readAllBytes(Shader.class, "/org/jge/glsl/ShadowFragmentShader.glsl"),
            "aPosition"
        );

        int[] b = new int[1];

        Game.getGL().glGenBuffers(1, b, 0);
        vbo = b[0];
    }

    public void begin(Node node, Node light) {
        GL2 gl = Game.getGL();

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo);
        shader.begin();
        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, VERTEX_STRIDE * 4, 0);
        shader.set("uProjection", light.calcLightProjection(matrix));
        shader.set("uView", light.calcLightView(matrix));
        shader.set("uModel", node.model);
        shader.set("uLightPosition", light.absolutePosition);
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void push(Vector<VertexPTN> vertices, Vector<Integer> indices, int indexCount) {
        if(vBuf.position() + indices.size() * VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + indices.size() * VERTEX_STRIDE;

            System.out.println("increasing shadow renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = Buffers.newDirectFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        for(int i = 0; i != indexCount; i++) {
            VertexPTN v = vertices.get(indices.get(i));

            vBuf.put(v.position.x);
            vBuf.put(v.position.y);
            vBuf.put(v.position.z);
        }
    }

    public void push(VertexPTN v) {
        if(vBuf.position() + VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + 600 * VERTEX_STRIDE;

            System.out.println("increasing shadow renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = Buffers.newDirectFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }

        vBuf.put(v.position.x);
        vBuf.put(v.position.y);
        vBuf.put(v.position.z);
    }

    public void end() {
        GL2 gl = Game.getGL();
        int count = vBuf.position() / VERTEX_STRIDE;

        if(count > 0) {
            vBuf.flip();
            gl.glBufferData(GL2.GL_ARRAY_BUFFER, count * VERTEX_STRIDE * 4, vBuf, GL2.GL_DYNAMIC_DRAW);
            gl.glDrawArrays(GL2.GL_TRIANGLES, 0, count);
        }
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        shader.end();
    }

    @Override
    public void destroy() throws Exception {
        shader.destroy();
        Game.getGL().glDeleteBuffers(1, new int[] { vbo }, 0);
        super.destroy();
    }
}
