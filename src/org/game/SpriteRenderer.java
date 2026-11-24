package org.game;

public class SpriteRenderer extends Resource {
    
    private Shader shader;
    private VertexArrayBuffer vertexBuffer;
    private Mat4 projection = new Mat4();
    private Texture texture = null;
    private VertexAttribute[] attributes =                     
        new VertexAttribute[] {
            new VertexAttribute("iPosition", 2),
            new VertexAttribute("iTextureCoordinate", 2),
            new VertexAttribute("iColor", 4)
        };

    public SpriteRenderer() throws Exception {
        shader =  
            new Shader(
                "#version 150 core\n" +
                "in vec2 iPosition;\n" +
                "in vec2 iTextureCoordinate;\n" +
                "in vec4 iColor;\n" +
                "out vec2 vTextureCoordinate;\n" +
                "out vec4 vColor;\n" +
                "uniform mat4 uProjection;\n" +
                "void main() {\n" +
                "\tgl_Position = uProjection * vec4(iPosition, 0.0, 1.0);\n" +
                "\tvTextureCoordinate = iTextureCoordinate;\n" +
                "\tvColor = iColor;\n" +
                "}\n",
                "#version 150 core\n" +
                "in vec2 vTextureCoordinate;\n" +
                "in vec4 vColor;\n" +
                "out vec4 oColor;\n" +
                "uniform sampler2D uTexture;\n" +
                "void main() {\n" +
                "\toColor = texture(uTexture, vTextureCoordinate) * vColor;\n" +
                "}\n", attributes
            );

        vertexBuffer = new VertexArrayBuffer(6 * 1000 * shader.getStride());
    }

    public void setProjection(int width, int height) {
        projection.identity().orthoLH(0, width, height, 0, -1, 1);
        texture = null;
    }

    public void begin(Texture texture) {
        if(this.texture != null) {
            return ;
        }
        this.texture = texture;
        if(texture != null) {
            vertexBuffer.begin(0);
            shader.begin();
            shader.set("uProjection", projection);
            shader.set("uTexture", texture, 0);
        }
    }

    public void push(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2, boolean flip) {
        if(texture == null) {
            return;
        }

        float tw = texture.width;
        float th = texture.height;
        float sx1 = sx / tw;
        float sy1 = sy / th;
        float sx2 = (sx + sw) / tw;
        float sy2 = (sy + sh) / th;
        float dx1 = dx;
        float dy1 = dy;
        float dx2 = dx + dw;
        float dy2 = dy + dh;

        if(flip) {
            float tmp = sy1;

            sy1 = sy2;
            sy2 = tmp;
        }

        vertexBuffer.push(dx1, dy1);
        vertexBuffer.push(sx1, sy1);
        vertexBuffer.push(r1, g1, b1, a1);

        vertexBuffer.push(dx1, dy2);
        vertexBuffer.push(sx1, sy2);
        vertexBuffer.push(r2, g2, b2, a2);

        vertexBuffer.push(dx2, dy2);
        vertexBuffer.push(sx2, sy2);
        vertexBuffer.push(r2, g2, b2, a2);

        vertexBuffer.push(dx2, dy2);
        vertexBuffer.push(sx2, sy2);
        vertexBuffer.push(r2, g2, b2, a2);

        vertexBuffer.push(dx2, dy1);
        vertexBuffer.push(sx2, sy1);
        vertexBuffer.push(r1, g1, b1, a1);

        vertexBuffer.push(dx1, dy1);
        vertexBuffer.push(sx1, sy1);
        vertexBuffer.push(r1, g1, b1, a1);
    }

    public void push(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, Vec4 color1, Vec4 color2, boolean flip) {
        push(sx, sy, sw, sh, dx, dy, dw, dh, color1.x, color1.y, color1.z, color1.w, color2.x, color2.y, color2.z, color2.w, flip);
    }

    public void push(String text, int columns, int characterWidth, int characterHeight, int scale, int lineSpacing, int x, int y, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        int sx = x;

        for(int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);

            if(c == '\n') {
                x = sx;
                y += lineSpacing * scale + characterHeight * scale;
            } else {
                int j = (int)c - (int)' ';

                if(j >= 0 && j < 100) {
                    int col = j % columns;
                    int row = j / columns;

                    push(col * characterWidth, row * characterHeight, characterWidth, characterHeight, x, y, characterWidth * scale, characterHeight * scale, r1, g1, b1, a1, r2, g2, b2, a2, false);
                    x += characterWidth * scale;
                }
            }
        } 
    }

    public void push(String text, int columns, int characterWidth, int characterHeight, int scale, int lineSpacing, int x, int y, Vec4 color1, Vec4 color2) {
        push(text, columns, characterWidth, characterHeight, scale, lineSpacing, x, y, color1.x, color1.y, color1.z, color1.w, color2.x, color2.y, color2.z, color2.w);
    }

    public void end() throws Exception {
        if(texture != null) {
            vertexBuffer.end(BufferUsage.STREAM);
            shader.draw(GeometryType.TRIANGLES, vertexBuffer);
            shader.end();
            texture = null;
        }
    }

    @Override
    public void destroy() {
        shader.destroy();
        vertexBuffer.destroy();
        super.destroy();
    }
}
