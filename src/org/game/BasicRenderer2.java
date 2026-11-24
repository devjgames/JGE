package org.game;

public class BasicRenderer2 extends BasicRenderer {

    public  BasicRenderer2() throws Exception {
    }

    @Override
    public void begin(int width, int height, Vec4 backgroundColor) throws Exception {
        super.begin(width, height, backgroundColor);

        setWarp(false, 0, 2, 0.25f, true);
        setDecal(null);
    }

    public void setWarp(boolean enabled, float time, float amplitude, float frequency, boolean warpY) {
        getShader().set("uWarp", enabled);
        getShader().set("uWarpTime", time);
        getShader().set("uWarpAmplitude", amplitude);
        getShader().set("uWarpFrequency", frequency);
        getShader().set("uWarpY", warpY);
    }

    public void setDecal(Texture decal) {
        getShader().set("uDecalEnabled", decal != null);
        if(decal != null) {
            getShader().set("uDecal", decal, 1);
        }
    }

    @Override
    protected String getCustomVertexUniforms() {
        return 
            "uniform int uWarp;\n" +
            "uniform float uWarpTime;\n" + 
            "uniform float uWarpAmplitude;\n" +
            "uniform float uWarpFrequency;\n" +
            "uniform int uWarpY;\n";

    }

    @Override
    protected String getCustomVertexMain() {
        return 
            "\tif(uWarp != 0) {\n" +
            "\t\tposition.x = position.x + uWarpAmplitude * cos(position.z * uWarpFrequency + uWarpTime) * sin(position.y * uWarpFrequency + uWarpTime);\n" +
            "\t\tposition.y = position.y + uWarpAmplitude * cos(position.x * uWarpFrequency + uWarpTime) * sin(position.z * uWarpFrequency + uWarpTime) * float(uWarpY);\n" +
            "\t\tposition.z = position.z + uWarpAmplitude * cos(position.y * uWarpFrequency + uWarpTime) * sin(position.x * uWarpFrequency + uWarpTime);\n" +
            "\t}\n";
    }

    @Override
    protected String getCustomFragmentUniforms() {
        return 
            "uniform sampler2D uDecal;\n" +
            "uniform int uDecalEnabled;\n";
    }

    @Override
    protected String getCustomFragmentMain() {
        return
            "\tif(uDecalEnabled != 0) {\n" +
            "\t\tvec4 c = texture(uDecal, vTextureCoordinate);\n" +
            "\t\tcolor.rgb = (1.0 - c.a) * color.rgb + c.a * c.rgb;\n" +
            "\t}\n";
    }
}
