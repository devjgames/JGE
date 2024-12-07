package org.jge;


import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;


public class SpriteRenderer extends Renderer {

    public static final int VERTEX_STRIDE = 8;
    
    private final Shader shader;
    private final  int vao;
    private final int vbo;
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(6 * VERTEX_STRIDE);
    private Texture texture = null;
    private final Matrix4f matrix = new Matrix4f();

    public SpriteRenderer() throws Exception {
        shader = new Shader(
            IO.readAllBytes(Shader.class, "/SpriteVertexShader.glsl"),
            IO.readAllBytes(Shader.class, "/SpriteFragmentShader.glsl"),
            "aPosition", "aTextureCoordinate", "aColor"
        );
        
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
    }

    public void begin() {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        shader.begin();
        shader.set("uProjection", matrix.identity().ortho(0, Game.getInstance().getFrameWidth(), Game.getInstance().getFrameHeight(), 0, -1, 1));
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, VERTEX_STRIDE * 4, 4 * 4);

        texture = null;

        GFX.setDepthState(DepthState.NONE);
        GFX.setBlendState(BlendState.ALPHA);
        GFX.setCullState(CullState.NONE);
    }

    public void beginSprite(Texture texture) {
        shader.bind(GL11.GL_TEXTURE_2D, "uTexture", 0, (this.texture = texture).id);

        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void push(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, float r, float g, float b, float a, boolean flip) {
        if(texture == null ){
            return;
        }

        float tw = texture.w;
        float th = texture.h;
        float sx1 = sx / tw;
        float sy1 = sy / th;
        float sx2 = (sx + sw) / tw;
        float sy2 = (sy + sh) / th;
        float dx1 = dx;
        float dy1 = dy;
        float dx2 = dx + dw;
        float dy2 = dy + dh;

        if(vBuf.position() + 6 * VERTEX_STRIDE > vBuf.capacity()) {
            int newCapacity = vBuf.position() + 600 * VERTEX_STRIDE;

            System.out.println("increasing sprite renderer vertex buffer capacity to " + newCapacity);

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(newCapacity);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }

        if(flip) {
            float t = sy1;

            sy1 = sy2;
            sy2 = t;
        }

        vBuf.put(dx1);
        vBuf.put(dy1);
        vBuf.put(sx1);
        vBuf.put(sy1);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);

        vBuf.put(dx1);
        vBuf.put(dy2);
        vBuf.put(sx1);
        vBuf.put(sy2);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);

        vBuf.put(dx2);
        vBuf.put(dy2);
        vBuf.put(sx2);
        vBuf.put(sy2);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);

        vBuf.put(dx2);
        vBuf.put(dy2);
        vBuf.put(sx2);
        vBuf.put(sy2);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);

        vBuf.put(dx2);
        vBuf.put(dy1);
        vBuf.put(sx2);
        vBuf.put(sy1);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);

        vBuf.put(dx1);
        vBuf.put(dy1);
        vBuf.put(sx1);
        vBuf.put(sy1);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);
    }

    public void push(String text, int scale, int cw, int ch, int cols, int lineSpacing, int x, int y, float r, float g, float b, float a) {
        int sx = x;

        for(int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);

            if(c == '\n') {
                x = sx;
                y += lineSpacing * scale + ch * scale;
            } else {
                int j = (int)c - (int)' ';

                if(j >= 0 && j < 100) {
                    int col = j % cols;
                    int row = j / cols;

                    push(col * cw, row * ch, cw, ch, x, y, cw * scale, ch * scale, r, g, b, a, false);
                    x += cw * scale;
                }
            }
        }
    }

    public void endSprite() {
        int count = vBuf.position() / VERTEX_STRIDE;

        if(count > 0) {
            vBuf.flip();

            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STREAM_DRAW);
            GL15.glDrawArrays(GL11.GL_TRIANGLES, 0, count);
        }
    }

    public void end() {
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
