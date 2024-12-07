package org.jge;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class RenderTarget extends Resource {
    
    public final Texture texture;

    private final int framebuffer;
    private final int renderbuffer;
    private final int[] viewport = new int[4];

    public RenderTarget(int w, int h, ColorFormat format) throws Exception {
        texture = new Texture(w, h, format);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id);
        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        renderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32F, w, h);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderbuffer);;
        GL30.glFramebufferTexture1D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture.id, 0);
        GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);;

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        if(status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("failed to allocated render target");
        }
    }

    public void begin() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        GL11.glViewport(0, 0, texture.w, texture.h);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
    }

    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }


    @Override
    public void destroy() throws Exception {
        texture.destroy();
        GL30.glDeleteFramebuffers(framebuffer);
        GL30.glDeleteRenderbuffers(renderbuffer);

        super.destroy();
    }
}
