package org.jge;

import org.joml.Matrix4f;

import com.jogamp.opengl.GL2;

public class LineRenderer extends Resource {
    

    private final VertexPTC vertex = new VertexPTC();
    private final ColorRenderer renderer;

    public LineRenderer() throws Exception {
        renderer = new ColorRenderer();
    }

    public void begin(Matrix4f projection, Matrix4f view, Matrix4f model) {
        renderer.begin(projection, view, model, null);
    }

    public void push(float x1, float y1, float z1, float r1, float g1, float b1, float a1, float x2, float y2, float z2, float r2, float g2, float b2, float a2) {
        vertex.position.set(x1, y1, z1);
        vertex.color.set(r1, g1, b1, a1);
        renderer.push(vertex);

        vertex.position.set(x2, y2, z2);
        vertex.color.set(r2, g2, b2, a2);
        renderer.push(vertex);
    }

    public void end() {
        renderer.end(GL2.GL_LINES);
    }

    @Override
    public void destroy() throws Exception {
        renderer.destroy();
        super.destroy();
    }
}
