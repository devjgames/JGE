package org.game;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class RenderTarget extends Resource {
    
    private int fbo;
    private int rbo;
    private Texture[] textures;
    private int width;
    private int height;
    private boolean depthBuffer;
    private int[] viewport = new int[4];

    public RenderTarget(int width, int height, boolean depthBuffer, ColorFormat ... formats) throws Exception {
        Log.getInstance().put(1, "creating render target - " + width + " x " + height + " ...");
        if(formats.length == 0) {
            throw new Exception("render target must have at least one color format");
        }
        this.width = width;
        this.height = height;

        fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);

        int[] drawBuffers = new int[formats.length];

        textures = new Texture[formats.length];
        for(int i = 0; i != textures.length; i++) {
            textures[i] = new Texture(width, height, formats[i], null);
            textures[i].bind();
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL30.GL_TEXTURE_2D, textures[i].id, 0);
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
        }

        this.depthBuffer = depthBuffer;

        if(depthBuffer) {
            rbo = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32F, width, height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);
        }
        GL30.glDrawBuffers(drawBuffers);

        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("failed to create render target");
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextureCount() {
        return textures.length;
    }

    public Texture getTexture(int i) {
        return textures[i];
    }

    public void begin() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        GL11.glViewport(0, 0, width, height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
    }

    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    @Override
    public void destroy() {
        GL30.glDeleteFramebuffers(fbo);
        if(depthBuffer) {
            GL30.glDeleteRenderbuffers(rbo);
        }
        for(Texture texture : textures) {
            texture.destroy();
        }
        super.destroy();
    }
}
