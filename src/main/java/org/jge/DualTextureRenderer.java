package org.jge;

import java.nio.FloatBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DualTextureRenderer extends Renderer {
    
    public static final int VERTEX_STRIDE = 7;
    
    private final Shader shader;
    private final int vao;
    private final int vbo;
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(6 * VERTEX_STRIDE);

    public DualTextureRenderer() throws Exception {
        shader = new Shader(
            IO.readAllBytes(Shader.class, "/DualTextureVertexShader.glsl"), 
            IO.readAllBytes(Shader.class, "/DualTextureFragmentShader.glsl"),
            "aPosition", "aTextureCoordinate", "aTextureCoordinate2"
        );

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
    }

    public void begin(Matrix4f projection, Matrix4f view, Matrix4f model, Texture texture, Texture texture2, Texture decal, Vector4f color) {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        shader.begin();
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 3);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 5);
        shader.set("uProjection", projection);
        shader.set("uView", view);
        shader.set("uModel", model);
        shader.set("uColor", color);
        shader.set("uTextureEnabled", texture != null);
        if(texture != null) {
            shader.bind(GL11.GL_TEXTURE_2D, "uTexture", 0, texture.id);
        }
        shader.set("uTexture2Enabled", texture2 != null);
        if(texture != null) {
            shader.bind(GL11.GL_TEXTURE_2D, "uTexture2", 1, texture2.id);
        }
        shader.set("uDecalTextureEnabled", decal != null);
        if(decal != null) {
            shader.bind(GL11.GL_TEXTURE_2D, "uDecalTexture", 2, decal.id);
        }
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void push(Vector<VertexPT2N> vertices, Vector<Integer> indices, int indexCount) {
        if(vBuf.position() + indices.size() * VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + indices.size() * VERTEX_STRIDE;

            System.out.println("increasing dual texture renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        for(int i = 0; i != indexCount; i++) {
            VertexPT2N v = vertices.get(indices.get(i));

            vBuf.put(v.position.x);
            vBuf.put(v.position.y);
            vBuf.put(v.position.z);
            vBuf.put(v.textureCoordinate.x);
            vBuf.put(v.textureCoordinate.y);
            vBuf.put(v.textureCoordinate2.x);
            vBuf.put(v.textureCoordinate2.y);
        }
    }

    public void end() {
        int count = vBuf.position() / VERTEX_STRIDE;

        if(count > 0) {
            vBuf.flip();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_DYNAMIC_DRAW);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count);
        }
        shader.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy() throws Exception {
        shader.destroy();
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        super.destroy();
    }
}
