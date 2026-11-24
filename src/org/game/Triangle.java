package org.game;

import org.joml.GeometryUtils;

public class Triangle {
    
    public final Vec3 p1 = new Vec3();
    public final Vec3 p2 = new Vec3();
    public final Vec3 p3 = new Vec3();
    public final Vec3 n = new Vec3();
    public float d = 0;

    public Triangle() {
    }

    public void calcPlane() {
        GeometryUtils.normal(p1, p2, p3, n);
        n.normalize();
        d = -p1.dot(n);
    }

    public void buffer(float amount) {
        // Edge directions
        float e1x = p2.x - p1.x, e1y = p2.y - p1.y, e1z = p2.z - p1.z;
        float e2x = p3.x - p2.x, e2y = p3.y - p2.y, e2z = p3.z - p2.z;
        float e3x = p1.x - p3.x, e3y = p1.y - p3.y, e3z = p1.z - p3.z;

        // Outward in-plane unit normal of each edge: (edge x n) normalized.
        // Assumes calcPlane() has been called and the winding matches it.
        float a1x = e1y * n.z - e1z * n.y, a1y = e1z * n.x - e1x * n.z, a1z = e1x * n.y - e1y * n.x;
        float l1 = 1f / (float) Math.sqrt(a1x * a1x + a1y * a1y + a1z * a1z);
        a1x *= l1; a1y *= l1; a1z *= l1;

        float a2x = e2y * n.z - e2z * n.y, a2y = e2z * n.x - e2x * n.z, a2z = e2x * n.y - e2y * n.x;
        float l2 = 1f / (float) Math.sqrt(a2x * a2x + a2y * a2y + a2z * a2z);
        a2x *= l2; a2y *= l2; a2z *= l2;

        float a3x = e3y * n.z - e3z * n.y, a3y = e3z * n.x - e3x * n.z, a3z = e3x * n.y - e3y * n.x;
        float l3 = 1f / (float) Math.sqrt(a3x * a3x + a3y * a3y + a3z * a3z);
        a3x *= l3; a3y *= l3; a3z *= l3;

        // Each vertex moves along the sum of its two adjacent edge normals, scaled so
        // that both edges shift by exactly `amount`: k = amount / (1 + na.nb)
        // p1 is shared by edges 3 and 1
        float k1 = amount / (1f + a3x * a1x + a3y * a1y + a3z * a1z);
        // p2 is shared by edges 1 and 2
        float k2 = amount / (1f + a1x * a2x + a1y * a2y + a1z * a2z);
        // p3 is shared by edges 2 and 3
        float k3 = amount / (1f + a2x * a3x + a2y * a3y + a2z * a3z);

        p1.x += k1 * (a3x + a1x); p1.y += k1 * (a3y + a1y); p1.z += k1 * (a3z + a1z);
        p2.x += k2 * (a1x + a2x); p2.y += k2 * (a1y + a2y); p2.z += k2 * (a1z + a2z);
        p3.x += k3 * (a2x + a3x); p3.y += k3 * (a2y + a3y); p3.z += k3 * (a2z + a3z);

        // The offset stays in the triangle's plane, so n and d remain valid.
    }
}
