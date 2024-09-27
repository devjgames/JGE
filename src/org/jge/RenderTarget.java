package org.jge;

import com.jogamp.opengl.GL2;

public class RenderTarget extends Resource {
    
    public final Texture texture;

    private final int framebuffer;
    private final int renderbuffer;
    private final int[] viewport = new int[4];

    public RenderTarget(int w, int h, ColorFormat format) throws Exception {
        GL2 gl = Game.getGL();

        int[] framebufferArray = new int[1];
        int[] renderbufferArray = new int[1];

        texture = new Texture(w, h, format);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.id);
        gl.glGenFramebuffers(1, framebufferArray, 0);
        framebuffer = framebufferArray[0];
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
        gl.glGenRenderbuffers(1, renderbufferArray, 0);
        renderbuffer = renderbufferArray[0];
        gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderbuffer);
        gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32F, w, h);
        gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, renderbuffer);
        gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, texture.id, 0);
        gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0);

        int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
        gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        if(status != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("failed to allocated render target");
        }
    }

    public void begin() {
        GL2 gl = Game.getGL();

        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        gl.glViewport(0, 0, texture.w, texture.h);
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
    }

    public void end() {
        GL2 gl = Game.getGL();

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
        gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }


    @Override
    public void destroy() throws Exception {
        GL2 gl = Game.getGL();
        
        texture.destroy();
        gl.glDeleteFramebuffers(1, new int[] { framebuffer }, 0);
        gl.glDeleteRenderbuffers(1, new int[] { renderbuffer }, 0);

        super.destroy();
    }
}
