package org.jge;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL2;

public class Texture extends Resource {

    public static class Loader implements AssetLoader {

        @Override
        public Object load(File file, AssetManager assets) throws Exception {
            BufferedImage image = ImageIO.read(file);
            int w = image.getWidth();
            int h = image.getHeight();
            int[] pixels = new int[w * h];
            byte[] rgba = new byte[w * h * 4];

            image.getRGB(0, 0, w, h, pixels, 0, w);

            for(int x = 0; x != w; x++) {
                for(int y = 0; y != h; y++) {
                    int i = y * w + x;
                    int j = y * w * 4 + x * 4;
                    int p = pixels[i];
                    int r = (p >> 16) & 0xFF;
                    int g = (p >> 8) & 0xFF;
                    int b = p & 0xFF;
                    int a = (p >> 24) & 0xFF;

                    rgba[j++] = (byte)r;
                    rgba[j++] = (byte)g;
                    rgba[j++] = (byte)b;
                    rgba[j] = (byte)a;
                }
            }
            return new Texture(file, w, h, rgba);
        }
    }
    
    public final File file;
    public final int id;
    public final int w;
    public final int h;

    public Texture(File file, int w, int h, byte[] rgba) throws Exception {
        ByteBuffer buf = ByteBuffer.allocateDirect(rgba.length).order(ByteOrder.nativeOrder());
        GL2 gl = Game.getGL();

        buf.put(rgba);
        buf.flip();

        this.file = file;
        this.w = w;
        this.h = h;

        int[] v = new int[1];

        gl.glGenTextures(1, v, 0);
        id = v[0];
        gl.glBindTexture(GL2.GL_TEXTURE_2D, id);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
        gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, w, h, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buf);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    public Texture(int w, int h, ColorFormat format) {
        GL2 gl = Game.getGL();

        this.file = null;
        this.w = w;
        this.h = h;

        int[] v = new int[1];

        gl.glGenTextures(1, v, 0);
        id = v[0];
        gl.glBindTexture(GL2.GL_TEXTURE_2D, id);
        if(format == ColorFormat.COLOR) {
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        } else {
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        }
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        if(format == ColorFormat.COLOR) {
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, w, h, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        } else if(format == ColorFormat.FLOAT) {
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_R32F, w, h, 0, GL2.GL_RED, GL2.GL_FLOAT, (FloatBuffer)null);
        } else {
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, w, h, 0, GL2.GL_RGBA, GL2.GL_FLOAT, (FloatBuffer)null);
        }
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    @Override
    public void destroy() throws Exception {
        Game.getGL().glDeleteTextures(1, new int[] { id }, 0);
        super.destroy();
    }
}
