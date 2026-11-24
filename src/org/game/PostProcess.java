package org.game;

public class PostProcess extends Resource {

    private RenderTarget target = null;
    private Shader shader;
    private VertexArrayBuffer vertexBuffer;
    private VertexElementBuffer vertexElementBuffer;

    public PostProcess(String fragmentSource, String fragmentInputName) throws Exception {
        shader = new Shader(
            "#version 150 core\n" +
            "in vec2 iPosition;\n" +
            "out vec2 " + fragmentInputName + ";\n" +
            "void main() {\n" +
            "\tgl_Position = vec4(iPosition * 2.0 - vec2(1.0, 1.0), 0.0, 1.0);\n" +
            "\t" + fragmentInputName + " = " + "iPosition;\n" +
            "}",
            fragmentSource,
            new VertexAttribute("iPosition", 2)
        );

        vertexBuffer = new VertexArrayBuffer(0);
        vertexBuffer.begin(2 * 4);
        vertexBuffer.push(0, 0);
        vertexBuffer.push(1, 0);
        vertexBuffer.push(1, 1);
        vertexBuffer.push(0, 1);
        vertexBuffer.end(BufferUsage.STATIC);

        vertexElementBuffer = new VertexElementBuffer(0);
        vertexElementBuffer.begin(6);
        vertexElementBuffer.push(0, 1, 2, 3);
        vertexElementBuffer.end(BufferUsage.STATIC);
    }

    public Shader begin(Texture texture, Vec4 backgroundColor) throws Exception {
        return begin(texture, backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);
    }

    public Shader begin(Texture texture, float r, float g, float b, float a) throws Exception {
        boolean create = target == null;
        int w = texture.width;
        int h = texture.height;

        if(!create) {
            create = w > 1 && h > 1 && (w != target.getWidth() || h != target.getHeight());
        }
        if(create) {
            if(target != null) {
                target.destroy();
            }
            target = new RenderTarget(w, h, false, ColorFormat.COLOR);
        }
        target.begin();
        GFX.clear(r, g, b, a);
        GFX.setDepthState(DepthState.NONE);
        GFX.setBlendState(BlendState.ALPHA);
        GFX.setCullState(CullState.NONE);
        shader.begin();
        return shader;
    }

    public Texture end() throws Exception {
        shader.draw(GeometryType.TRIANGLES, vertexBuffer, vertexElementBuffer);
        shader.end();
        target.end();

        return  target.getTexture(0);
    }

    @Override
    public void destroy() {
        if(target != null) {
            target.destroy();
        }
        shader.destroy();
        vertexBuffer.destroy();
        vertexElementBuffer.destroy();
        super.destroy();
    }
}