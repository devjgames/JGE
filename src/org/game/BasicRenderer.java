package org.game;

public class BasicRenderer extends Resource {

    public static VertexAttribute[] getVertexAttributes() throws Exception {
        return
            new VertexAttribute[] {
                new VertexAttribute("iPosition", 3),
                new VertexAttribute("iTextureCoordinate", 2),
                new VertexAttribute("iNormal", 3),
                new VertexAttribute("iColor", 4)
            };
    }

    public static final int MAX_LIGHTS = 8;
    public static final int VERTEX_STRIDE = 12;

    private Shader shader;
    private RenderTarget renderTarget = null;
    private Mat4 matrix = new Mat4();
    private int trianglesRendered = 0;

    public BasicRenderer() throws Exception {
        shader = new Shader(
            "#version 150 core\n" +
            "in vec3 iPosition;\n" +
            "in vec2 iTextureCoordinate;\n" +
            "in vec3 iNormal;\n" +
            "in vec4 iColor;\n" +
            "out vec3 vPosition;\n" +
            "out vec2 vTextureCoordinate;\n" +
            "out vec3 vNormal;\n" +
            "out vec4 vColor;\n" +
            "uniform mat4 uProjection;\n" +
            "uniform mat4 uView;\n" +
            "uniform mat4 uModel;\n" +
            "uniform mat4 uModelIT;\n" +
            getCustomVertexUniforms() + "\n" +
            "void main() {\n" +
            "\tvec4 position = (uModel * vec4(iPosition, 1.0));\n" +
            getCustomVertexMain() + "\n" +
            "\tgl_Position = uProjection * uView * position;\n" +
            "\tvPosition = position.xyz;\n" +
            "\tvTextureCoordinate = iTextureCoordinate;\n" +
            "\tvNormal = normalize((uModelIT * vec4(iNormal, 0.0)).xyz);\n" +
            "\tvColor = iColor;\n" +
            "}\n",
            "#version 150 core\n" +
            "#define MAX_LIGHTS " + MAX_LIGHTS + "\n" +
            "in vec3 vPosition;\n" +
            "in vec2 vTextureCoordinate;\n" +
            "in vec3 vNormal;\n" +
            "in vec4 vColor;\n" +
            "uniform vec3 uLightPosition[MAX_LIGHTS];\n" +
            "uniform vec4 uLightColor[MAX_LIGHTS];\n" +
            "uniform float uLightRadius[MAX_LIGHTS];\n" +
            "uniform int uLightCount;\n" +
            "uniform vec4 uAmbientColor;\n" +
            "uniform vec4 uDiffuseColor;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform int uTextureEnabled;\n" +
            "uniform int uLightingEnabled;\n"+
            getCustomFragmentUniforms() + "\n" +
            "out vec4 oColor;\n" +
            "void main() {\n" +
            "\tvec3 position = vPosition;\n" +
            "\tvec3 normal = normalize(vNormal);\n" +
            "\tvec4 color = vColor;\n" +
            "\tif(uLightingEnabled != 0) {\n" +
            "\t\tcolor = uAmbientColor;\n" +
            "\t\tfor(int i = 0; i != uLightCount; i++) {\n" +
            "\t\t\tvec3 x = uLightPosition[i] - position;\n" +
            "\t\t\tfloat d = clamp(dot(normalize(x), normal), 0.0, 1.0);\n" +
            "\t\t\tfloat a = 1.0 - clamp(length(x) / uLightRadius[i], 0.0, 1.0);\n" +
            "\t\t\tcolor += a * d * uDiffuseColor * uLightColor[i];\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\tif(uTextureEnabled != 0) {\n" +
            "\t\tcolor *= texture(uTexture, vTextureCoordinate);\n" +
            "\t}\n" +
            getCustomFragmentMain() + "\n" +
            "\toColor = color;\n" +
            "}\n",
            new String[] {
                "oColor"
            },
            getVertexAttributes()
        );
    }

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    public void begin(int width, int height, Vec4 backgroundColor) throws Exception {
        begin(width, height, backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);
    }

    public void begin(int width, int height, float r, float g, float b, float a) throws Exception {
        boolean create = renderTarget == null;

        trianglesRendered = 0;

        if(!create) {
            create = width > 10 && height > 10 && (renderTarget.getWidth() != width || renderTarget.getHeight() != height);
        }
        if(create) {
            if(renderTarget != null) {
                renderTarget.destroy();
            }
            renderTarget = new RenderTarget(width, height, true, ColorFormat.COLOR);
        }
        renderTarget.begin();
        GFX.clear(r, g, b, a);
        shader.begin();
        setProjection((Mat4)matrix.identity());
        setView(matrix);
        setModel(matrix);
        setModelIT(matrix);
        setAmbientColor(0.2f, 0.2f, 0.2f, 1);
        setDiffuseColor(1, 1, 1, 1);
        setTexture(null);
        setLightCount(0);
        setLightingEnabled(true);
        for(int i = 0; i != MAX_LIGHTS; i++) {
            setLightPosition(i, 0, 0, 0);
            setLightColor(i, 1, 1, 1, 1);
            setLightRadius(i, 300);
        }
    }

    public void setProjection(Mat4 projection) {
        shader.set("uProjection", projection);
    }

    public void setView(Mat4 view) {
        shader.set("uView", view);
    }

    public void setModel(Mat4 model) {
        shader.set("uModel", model);
    }

    public void setModelIT(Mat4 modelIT) {
        shader.set("uModelIT", modelIT);
    }

    public void setLightingEnabled(boolean enabled) {
        shader.set("uLightingEnabled", enabled);
    }

    public void setAmbientColor(float r, float g, float b, float a) {
        shader.set("uAmbientColor", r, g, b, a);
    }

    public void setAmbientColor(Vec4 color) {
        shader.set("uAmbientColor", color);
    }

    public void setDiffuseColor(float r, float g, float b, float a) {
        shader.set("uDiffuseColor", r, g, b, a);
    }

    public void setDiffuseColor(Vec4 color) {
        shader.set("uDiffuseColor", color);
    }

    public void setTexture(Texture texture) {
        shader.set("uTextureEnabled", texture != null);
        if(texture != null) {
            shader.set("uTexture", texture, 0);
        }
    }

    public void setLightCount(int count) throws Exception {
        if(count < 0 || count > MAX_LIGHTS) {
            throw new Exception("light count out of bounds");
        }
        shader.set("uLightCount", count);
    }

    public void setLightPosition(int i, float x, float y, float z) throws Exception {
        checkLightIndex(i);
        shader.set("uLightPosition[" + i + "]", x, y, z);
    }

    public void setLightPosition(int i, Vec3 position) throws Exception {
        checkLightIndex(i);
        shader.set("uLightPosition[" + i + "]", position);
    }

    public void setLightColor(int i, float r, float g, float b, float a) throws Exception {
        checkLightIndex(i);
        shader.set("uLightColor[" + i + "]", r, g, b, a);
    }

    public void setLightColor(int i, Vec4 color) throws Exception {
        checkLightIndex(i);
        shader.set("uLightColor[" + i + "]", color);
    }
    
    public void setLightRadius(int i, float radius) throws Exception {
        checkLightIndex(i);
        shader.set("uLightRadius[" + i + "]", radius);
    }

    public void draw(VertexArrayBuffer vertexBuffer) throws Exception {
        shader.draw(GeometryType.TRIANGLES, vertexBuffer);

        trianglesRendered += vertexBuffer.getCount() / VERTEX_STRIDE / 3;
    }

    public void draw(VertexArrayBuffer vertexBuffer, VertexElementBuffer vertexElementBuffer) throws Exception {
        shader.draw(GeometryType.TRIANGLES, vertexBuffer, vertexElementBuffer);

        trianglesRendered += vertexElementBuffer.getCount() / 3;
    }

    public void end() {
        shader.end();
        renderTarget.end();
    }

    protected String getCustomVertexUniforms() {
        return "";
    }

    protected String getCustomVertexMain() {
        return "";
    }

    protected String getCustomFragmentUniforms() {
        return "";
    }

    protected String getCustomFragmentMain() {
        return "";
    }

    protected Shader getShader() {
        return shader;
    }

    private void checkLightIndex(int i) throws Exception {
        if(i < 0 || i >= MAX_LIGHTS) {
            throw new Exception("light index out of bounds");
        }
    }

    @Override
    public void destroy() {
        shader.destroy();
        if(renderTarget != null) {
            renderTarget.destroy();
        }
        super.destroy();
    }
}
