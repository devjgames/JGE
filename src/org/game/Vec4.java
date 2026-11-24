package org.game;

import org.joml.Vector4f;

public class Vec4 extends Vector4f {
    
    public Vec4() {
    }

    public Vec4(float x, float y, float z, float w) {
        super(x, y, z, w);
    }

    public Vec4(Vec4 v) {
        super(v);
    }
}
