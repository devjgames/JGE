package org.game;

import org.joml.primitives.AABBf;

public class AABB extends AABBf {
    
    public AABB() {
        clear();
    }

    public AABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        super(x1, y1, z1, x2, y2, z2);
    }

    public AABB(Vec3 min, Vec3 max) {
        super(min, max);
    }

    public AABB(AABB b) {
        super(b);
    }

    public void clear() {
        setMax(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        setMin(+Float.MAX_VALUE, +Float.MAX_VALUE, +Float.MAX_VALUE);
    }

    public void buffer(float x, float y, float z) {
        if(!isEmpty()) {
            minX -= x;
            minY -= y;
            minZ -= z;
            maxX += x;
            maxY += y;
            maxZ += z;
        }
    }

    public boolean isEmpty() {
        return minX > maxX || minY > maxY || minZ > maxZ;
    }

    public boolean touches(AABB other) {
        if(!isEmpty() && !other.isEmpty()) {
            return !(
                minX > other.maxX || maxX < other.minX ||
                minY > other.maxY || maxY < other.minY ||
                minZ > other.maxZ || maxZ < other.minZ
            );
        }
        return false;
    }
}
