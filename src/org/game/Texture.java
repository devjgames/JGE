package org.game;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Texture extends Resource {

    static Texture load(File file) throws Exception {
        BufferedImage image = ImageIO.read(file);
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w * h];
        ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);

        image.getRGB(0, 0, w, h, pixels, 0, w);

        for(int y = 0; y != h; y++) {
            for(int x = 0; x != w; x++) {
                int i = y * w + x;
                int p = pixels[i];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                int a = (p >> 24) & 0xFF;

                buf.put((byte)r);
                buf.put((byte)g);
                buf.put((byte)b);
                buf.put((byte)a);
            }
        }
        buf.flip();

        return new Texture(w, h, ColorFormat.COLOR, buf);
    }
    
    public final int width;
    public final int height;

    final int id;

    public Texture(int w, int h, ColorFormat format, Buffer buf) {
        width = w;
        height = h;

        id = GL11.glGenTextures();
        bind();
        toNearest((format == ColorFormat.COLOR) ? false : true);
        if(format == ColorFormat.COLOR) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)buf);
        } else if(format == ColorFormat.FLOAT) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R32F, w, h, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)buf);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, w, h, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)buf);
        }
        unBind();
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public void toLinear(boolean clampToEdge) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
    }

    public void toNearest(boolean clampToEdge) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
    }

    public void save(File file) throws Exception {
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);

        int[] pixels = new int[width * height];
        byte[] rgba = new byte[width * height * 4];

        buf.get(rgba);

        for(int x = 0; x != width; x++) {
            for(int y = 0; y != height; y++) {
                int i = (height - y - 1) * width + x;
                int j = y * width * 4 + x * 4;
                int r = rgba[j++];
                int g = rgba[j++];
                int b = rgba[j];

                pixels[i] = 0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, width, height, pixels, 0, width);

        ImageIO.write(image, "PNG", file);
    }

    public void unBind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void destroy() {
        GL11.glDeleteTextures(id);
        super.destroy();
    }
}
