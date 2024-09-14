package org.jge;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class AABB {
    
    public final Vector3f min = new Vector3f();
    public final Vector3f max = new Vector3f();

    private final Vector2f r = new Vector2f();

    public AABB() {
        clear();
    }

    public AABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    public AABB(Vector3f min, Vector3f max) {
        set(min, max);
    }

    public AABB(AABB b) {
        set(b);
    }

    public boolean isEmpty() {
        return min.x > max.x || min.y > max.y || min.z > max.z;
    }

    public boolean contains(float x, float y, float z) {
        if(!isEmpty()) {
            return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
        }
        return false;
    }

    public boolean contains(Vector3f point) {
        return contains(point.x, point.y, point.z);
    }

    public boolean touches(AABB b) {
        if(!isEmpty() && !b.isEmpty()) {
            return !(
                b.min.x > max.x || b.max.x < min.x ||
                b.min.y > max.y || b.max.y < min.y ||
                b.min.z > max.z || b.max.z < min.z
            );
        }
        return false;
    }

    public Vector3f center(Vector3f center) {
        return max.add(min, center).div(2);
    }

    public Vector3f size(Vector3f size) {
        return max.sub(min, size);
    }

    public AABB clear() {
        min.set(1, 1, 1).mul(Float.MAX_VALUE);
        min.negate(max);

        return this;
    }

    public AABB set(float x1, float y1, float z1, float x2, float y2, float z2) {
        min.set(x1, y1, z1);
        max.set(x2, y2, z2);

        return this;
    }

    public AABB set(Vector3f min, Vector3f max) {
        this.min.set(min);
        this.max.set(max);

        return this;
    }

    public AABB set(AABB b) {
        min.set(b.min);
        max.set(b.max);

        return this;
    }

    public AABB buffer(float x, float y, float z) {
        if(!isEmpty()) {
            min.sub(x, y, z);
            max.add(x, y, z);
        }
        return this;
    }

    public AABB buffer(Vector3f amount) {
        return buffer(amount.x, amount.y, amount.z);
    }
    
    public AABB add(float x, float y, float z) {
        min.x = Math.min(x, min.x);
        min.y = Math.min(y, min.y);
        min.z = Math.min(z, min.z);
        max.x = Math.max(x, max.x);
        max.y = Math.max(y, max.y);
        max.z = Math.max(z, max.z);

        return this;
    }

    public AABB add(Vector3f point) {
        return add(point.x, point.y, point.z);
    }

    public AABB add(AABB b) {
        if(!b.isEmpty()) {
            add(b.min);
            add(b.max);
        }
        return this;
    }

    public AABB transform(Matrix4f m) {
        if(!isEmpty()) {
            m.transpose();

            float minx = m.m03();
            float miny = m.m13();
            float minz = m.m23();
            float maxx = minx;
            float maxy = miny;
            float maxz = minz;

            minx += (m.m00() < 0) ? m.m00() * max.x : m.m00() * min.x;
            minx += (m.m01() < 0) ? m.m01() * max.y : m.m01() * min.y;
            minx += (m.m02() < 0) ? m.m02() * max.z : m.m02() * min.z;
            maxx += (m.m00() > 0) ? m.m00() * max.x : m.m00() * min.x;
            maxx += (m.m01() > 0) ? m.m01() * max.y : m.m01() * min.y;
            maxx += (m.m02() > 0) ? m.m02() * max.z : m.m02() * min.z;

            miny += (m.m10() < 0) ? m.m10() * max.x : m.m10() * min.x;
            miny += (m.m11() < 0) ? m.m11() * max.y : m.m11() * min.y;
            miny += (m.m12() < 0) ? m.m12() * max.z : m.m12() * min.z;
            maxy += (m.m10() > 0) ? m.m10() * max.x : m.m10() * min.x;
            maxy += (m.m11() > 0) ? m.m11() * max.y : m.m11() * min.y;
            maxy += (m.m12() > 0) ? m.m12() * max.z : m.m12() * min.z;

            minz += (m.m20() < 0) ? m.m20() * max.x : m.m20() * min.x;
            minz += (m.m21() < 0) ? m.m21() * max.y : m.m21() * min.y;
            minz += (m.m22() < 0) ? m.m22() * max.z : m.m22() * min.z;
            maxz += (m.m20() > 0) ? m.m20() * max.x : m.m20() * min.x;
            maxz += (m.m21() > 0) ? m.m21() * max.y : m.m21() * min.y;
            maxz += (m.m22() > 0) ? m.m22() * max.z : m.m22() * min.z;

           set(minx, miny, minz, maxx, maxy, maxz);

            m.transpose();
        }
        return this;
    }

    public boolean isects(Vector3f origin, Vector3f direction, float[] time) {
        if(Intersectionf.intersectRayAab(origin, direction, min, max, r)) {
            if(r.x < 0 && r.y > 0) {
                time[0] = r.y;
                return true;
            } else if(r.x >= 0) {
                time[0] = r.x;
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return min + " -> " + max;
    }
}
