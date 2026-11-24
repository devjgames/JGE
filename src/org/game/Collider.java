package org.game;

import java.util.Vector;

public class Collider {

    public static interface ContactHandler {
    
        public void contactMade(TriangleSelector selector, Triangle triangle);
    }
    
    public Vec3 velocity = new Vec3();
    public float groundSlope = 60;
    public float roofSlope = 50;
    public float radius = 16;
    public final Vector<TriangleSelector> selectors = new Vector<>();
    public ContactHandler handler = null;

    private Mat4 groundMatrix = new Mat4();
    private Vec3 groundNormal = new Vec3();
    private boolean onGround;
    private boolean hitRoof;
    private Vec3 delta = new Vec3();
    private Vec3 normal = new Vec3();
    private Vec3 resolved = new Vec3();
    private Vec3 closest = new Vec3();
    private Vec3 cp = new Vec3();
    private Vec3 ab = new Vec3();
    private Vec3 ap = new Vec3();
    private Vec3 r = new Vec3();
    private Vec3 u = new Vec3();
    private Vec3 f = new Vec3();
    private float time = 0;
    private AABB bounds = new AABB();
    private boolean hit = false;
    private int tested = 0;
    private TriangleSelector hitSelector = null;
    private Triangle hitTriangle = null;

    public int getTested() {
        return tested;
    }

    public boolean didHitRoof() {
        return hitRoof;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void resolve(Vec3 position) {
        Game game = Game.getInstance();

        tested = 0;

        delta.set(velocity).mulDirection(groundMatrix).mul(game.getElapsedTime());
        if(delta.length() > radius * 0.5f) {
            delta.normalize().mul(radius * 0.5f);
        }
        position.add(delta);
        onGround = false;
        groundNormal.zero();
        groundMatrix.identity();

        for(int i = 0; i != 3; i++) {
            normal.zero();
            resolved.zero();
            hit = false;
            time = radius;
            bounds.minX = position.x - radius;
            bounds.minY = position.y - radius;
            bounds.minZ = position.z - radius;
            bounds.maxX = position.x + radius;
            bounds.maxY = position.y + radius;
            bounds.maxZ = position.z + radius;

            hitSelector = null;
            hitTriangle = null;

            for(TriangleSelector selector : selectors) {
                selector.select(bounds, false, (triangle, userData) -> {
                    float t = triangle.n.dot(-triangle.n.x, -triangle.n.y, -triangle.n.z);
                    boolean isects = Math.abs(t) > 0.0000001;

                    if(isects) {
                        t = (-triangle.d - position.dot(triangle.n)) / t;
                        isects = true;
                    } 

                    if(isects) {
                        float ix = position.x - triangle.n.x * t;
                        float iy = position.y - triangle.n.y * t;
                        float iz = position.z - triangle.n.z * t;
                        Vec3 p1 = triangle.p1;
                        Vec3 p2 = triangle.p2;
                        Vec3 p3 = triangle.p3;

                        if(t > 0.0000001 && t < time) {
                            if(Intersection.testPointInTriangle(ix, iy, iz, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z)) {
                                time = t;
                                normal.set(triangle.n);
                                resolved.set(ix, iy, iz).add(normal.x * radius, normal.y * radius, normal.z * radius);
                                hit = true;
                                hitSelector = selector;
                                hitTriangle = triangle;
                            } else {
                                closestPoint(triangle, position);
                                position.sub(closest, delta);
                                t = delta.length();
                                if(t > 0.0000001 && t < time) {
                                    time = t;
                                    normal.set(delta).normalize();
                                    resolved.set(closest).add(normal.x * radius, normal.y * radius, normal.z * radius);
                                    hit = true;
                                    hitSelector = selector;
                                    hitTriangle = triangle;
                                }
                             }
                        }
                    }
                    tested++;
                });
            }

            if(hit) {
                if(Math.acos(Math.max(-0.999, Math.min(0.999, normal.dot(0, 1, 0)))) < Math.toRadians(groundSlope)) {
                    onGround = true;
                    groundNormal.add(normal);
                    velocity.y = 0;
                } 
                if(Math.acos(Math.max(-0.999, Math.min(0.999, normal.dot(0, -1, 0)))) < Math.toRadians(roofSlope)) {
                    hitRoof = true;
                    velocity.y = 0;
                }
                position.set(resolved);

                if(handler != null) {
                    handler.contactMade(hitSelector, hitTriangle);
                }
            } else {
                break;
            }
        }

        if(onGround) {
            groundNormal.normalize(u);
            r.set(1, 0, 0);
            r.cross(u, f).normalize();
            u.cross(f, r).normalize();
            groundMatrix.set(
                r.x, r.y, r.z, 0,
                u.x, u.y, u.z, 0,
                f.x, f.y, f.z, 0,
                0, 0, 0, 1
            );
        }
    }

    private void closestPoint(Triangle triangle, Vec3 position) {
        float min = Float.MAX_VALUE;

        closest.set(triangle.p1);
        for(int i = 0; i != 3; i++) {
            Vec3 a = getPoint(triangle, i);
            Vec3 b = getPoint(triangle, i + 1);

            b.sub(a, ab);
            position.sub(a, ap);

            float s = ab.dot(ap);

            cp.set(a);

            if(s > 0) {
                s /= ab.lengthSquared();
                if(s < 1) {
                    cp.add(ab.mul(s));
                } else {
                    cp.set(b);
                }
            }
            position.sub(cp, delta);
            
            float l = delta.length();

            if(l < min) {
                min = l;
                closest.set(cp);
            }
        }
    }

    private Vec3 getPoint(Triangle triangle, int i) {
        if(i == 1) {
            return triangle.p2;
        } else if(i == 2) {
            return triangle.p3;
        }
        return triangle.p1;
    }
}
