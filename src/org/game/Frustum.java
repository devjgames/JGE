package org.game;

import org.joml.FrustumIntersection;

public class Frustum extends FrustumIntersection {

    public boolean testAab(AABB b) {
        int r = super.intersectAab(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);

        return r == FrustumIntersection.INSIDE || r == FrustumIntersection.INTERSECT;
    }
}
