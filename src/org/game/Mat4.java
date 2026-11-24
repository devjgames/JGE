package org.game;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;

public class Mat4 extends Matrix4f {
    
    public Mat4() {
        super();
    }

    public Mat4(FloatBuffer buf) {
        super(buf);
    }

    public Mat4(float[] a) {
        super(a);
    }

    public Mat4(Mat4 m) {
        super(m);
    }
}
