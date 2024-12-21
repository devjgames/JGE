package org.jge;

import org.joml.GeometryUtils;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Triangle {
    
    public final Vector3f p1 = new Vector3f();
    public final Vector3f p2 = new Vector3f();
    public final Vector3f p3 = new Vector3f();
    public final Vector3f n = new Vector3f();
    public float d  = 0;
    public int tag = 0;

    private final Vector3f n2 = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f a = new Vector3f();
    private final Vector3f b = new Vector3f();
    private final Vector3f p = new Vector3f();

    public Triangle() {
    }

    public Vector3f getPoint(int i) {
        if(i == 1) {
            return p2;
        } else if(i == 2) {
            return p3;
        } else {
            return p1;
        }
    }

    public Triangle setTag(int tag) {
        this.tag = tag;

        return this;
    }

    public Triangle set(Triangle triangle) {
        p1.set(triangle.p1);
        p2.set(triangle.p2);
        p3.set(triangle.p3);
        n.set(triangle.n);
        d = triangle.d;
        tag = triangle.tag;

        return this;
    }

    public Triangle calcPlane() {
        GeometryUtils.normal(p1, p2, p3, n);
        d = -p1.dot(n);

        return this;
    }

    public Triangle transform(Matrix4f m) {
        p1.mulPosition(m);
        p2.mulPosition(m);
        p3.mulPosition(m);

        return calcPlane();
    }

    public boolean contains(Vector3f point, float buffer) {
        for(int i = 0; i != 3; i++) {
            a.set(getPoint(i));
            b.set(getPoint(i + 1));
            b.sub(a, v);
            n.cross(v, n2).normalize(-buffer);
            a.add(n2);
            n.cross(v, n2).normalize();

            float d2 = -a.dot(n2);
            float s = n2.dot(point) + d2;

            if(s < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsPlane(Vector3f origin, Vector3f direction, float[] time) {
        float t = direction.dot(n);

        if(Math.abs(t) > 0.0000001) {
            t = (-d - origin.dot(n)) / t;
            if(t > 0.0000001 && t < time[0]) {
                time[0] = t;
                return true;
            }
        }
        return false;
    }

    public boolean intersects(Vector3f origin, Vector3f direction, float buffer, float[] time) {
        float t = time[0];

        if(intersectsPlane(origin, direction, time)) {
            direction.mul(time[0], p).add(origin);
            if(contains(p, buffer)) {
                return true;
            }
            time[0] = t;
        }
        return false;
    }

    public Vector3f closestPoint(Vector3f point, Vector3f closestPoint) {
        Intersectionf.findClosestPointOnTriangle(p1, p2, p3, point, closestPoint);
        return closestPoint;
    }

    @Override
    public String toString() {
        return p1 + " : " + p2 + " : " + p3 + " : " + n + " : " + d + " @ " + tag;
    }
}
