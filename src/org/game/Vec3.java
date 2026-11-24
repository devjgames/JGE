package org.game;

import org.joml.Vector3f;

public class Vec3 extends Vector3f {
    
    public Vec3() {
    }

    public Vec3(float x, float y, float z) {
        super(x, y, z);
    }

    public Vec3(Vec3 v) {
        super(v);
    }
}
