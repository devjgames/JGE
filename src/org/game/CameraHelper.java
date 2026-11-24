package org.game;

public class CameraHelper {
    
    private static Vec3 r = new Vec3();
    private static Vec3 u = new Vec3();
    private static Vec3 f = new Vec3();
    private static Vec3 x = new Vec3();
    private static Vec4 v = new Vec4();
    private static Mat4 m = new Mat4();

    public static void rotateAroundEye(Vec3 eye, Vec3 target, Vec3 up, float dx, float dy) {
        target.sub(eye, f);
        u.set(up);
        m.identity().rotate(dx, 0, 1, 0);
        f.cross(u, r).mulDirection(m).normalize();
        f.mulDirection(m);
        m.identity().rotate(dy, r);
        up.set(r.cross(f, u).mulDirection(m).normalize());
        f.mulDirection(m).normalize();
        target.set(eye).add(f);
    }

    public static void rotateAroundTarget(Vec3 eye, Vec3 target, Vec3 up, float dx, float dy) {
        eye.sub(target, f);
        u.set(up);
        m.identity().rotate(dx, 0, 1, 0);
        f.cross(u, r).mulDirection(m).normalize();
        f.mulDirection(m);
        m.identity().rotate(dy, r);
        up.set(r.cross(f, u).mulDirection(m).normalize());
        f.mulDirection(m);
        eye.set(target).add(f);
    }

    public static void move(Vec3 point, Vec3 eye, Vec3 target, float dx, float dy) {
        target.sub(eye, f);
        eye.sub(target, x);
        f.y = 0;
        if(f.length() > 0.0000001) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize();
            f.mul(dy);
            r.mul(dx);
            point.add(f).add(r);
        }
        target.add(x, eye);
    }

    public static void move(Vec3 point, Vec3 eye, Vec3 target, float dy) {
        eye.sub(target, x);
        point.y += dy;
        target.add(x, eye);
    }

    public static void zoom(Vec3 eye, Vec3 target, float dy) {
        float l;

        eye.sub(target, x);
        l = x.length();
        x.normalize().mul(l + dy);
        target.add(x, eye);
    }

    public static Vec3 unproject(float x, float y, float z, float vx, float vy, float vw, float vh, Mat4 projection, Mat4 view, Vec3 p) {
        m.set(projection).mul(view).invert();

        v.x = 2 * (x - vx) / vw - 1;
        v.y = 2 * (y - vy) / vh - 1;
        v.z = 2 * z - 1;
        v.w = 1;
        m.transform(v);
        v.div(v.w);

        return (Vec3)p.set(v.x, v.y, v.z);
    }
}
