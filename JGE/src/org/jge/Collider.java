package org.jge;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Collider {
    

    public static interface CollisionListener {
    
        void collided(Node node, Triangle triangle) throws Exception;
        
    }
    
    public float radius = 16;
    public int groundSlope = 60;
    public int roofSlope = 50;
    public final Vector3f velocity = new Vector3f();
    public CollisionListener collisionListener = null;

    private int tested = 0;
    private final Vector3f f = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f o = new Vector3f();
    private final Vector3f d = new Vector3f();
    private final Vector3f p = new Vector3f();
    private final Vector3f c = new Vector3f();
    private final AABB bounds = new AABB();
    private final Triangle triangle = new Triangle();
    private boolean onGround = false;
    private boolean hitRoof = false;
    private final Matrix4f groundMatrix = new Matrix4f();
    private final Vector3f delta = new Vector3f();
    private final Vector3f hNormal = new Vector3f();
    private final Vector3f groundNormal = new Vector3f();
    private final Triangle hTriangle = new Triangle();
    private Node hNode = null;
    private final Vector3f rPosition = new Vector3f();
    private final float[] time = new float[1];

    public boolean isOnGround() {
        return onGround;
    }

    public boolean didHitRoof() {
        return hitRoof;
    }

    public int getTested() {
        return tested;
    }

    public boolean intersect(Scene scene, Node root, Vector3f origin, Vector3f direction, float buffer, int mask, float[] time, boolean ignoreBackfaces, Triangle hit) throws Exception {
        hNode = null;
        root.traverse((n) -> {
            bounds.clear();
            bounds.add(origin);
            bounds.add(direction.mul(time[0], f).add(origin));
            if(n.bounds.touches(bounds)) {
                if(n.collidable) {
                    OctTree octTree = n.getOctTree(scene);

                    if(octTree != null) {
                        octTree.traverse((t) -> {
                            if(t.getBounds().touches(bounds)) {
                                for(int i = 0; i != t.getTriangleCount(); i++) {
                                    t.getTriangle(i, triangle);
                                    if((triangle.tag & mask) != 0) {
                                        boolean skip = ignoreBackfaces;
                                        
                                        if(skip) {
                                            skip = triangle.n.dot(direction) > 0;
                                        }
                                        if(!skip) {
                                            if(triangle.intersects(origin, direction, buffer, time)) {;
                                                hit.set(triangle);
                                                hNode = n;
                                            }
                                        }
                                    }
                                }
                                return true;
                            }
                            return false;
                        });
                    } else {
                        for(int i = 0; i != n.getTriangleCount(); i++) {
                            n.getTriangle(scene, i, triangle);
                            if((triangle.tag & mask) != 0) {
                                boolean skip = ignoreBackfaces;
                                        
                                if(skip) {
                                    skip = triangle.n.dot(direction) > 0;
                                }
                                if(!skip) {
                                    if(triangle.intersects(origin, direction, buffer, time)) {
                                        hit.set(triangle);
                                        hNode = n;
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        });
        return hNode != null;
    }

    public boolean resolve(Scene scene, Node root, Vector3f position) throws Exception {
        boolean collided = false;

        velocity.mul(Game.getInstance().elapsedTime(), delta);
        if(delta.length() > radius * 0.5f) {
            delta.normalize(radius * 0.5f);
        }
        delta.mulDirection(groundMatrix);
        position.add(delta);
        groundMatrix.identity();
        onGround = false;
        groundNormal.zero();
        tested = 0;
        for(int i = 0; i != 3; i++) {
            hNode = null;
            hNormal.zero();
            bounds.min.set(position).sub(radius, radius, radius);
            bounds.max.set(position).add(radius, radius, radius);
            time[0] = radius;
            root.traverse((n) -> {
                if(n.bounds.touches(bounds)) {
                    if(n.collidable) {
                        OctTree tree = n.getOctTree(scene);

                        if(tree != null) {
                            tree.traverse((t) -> {
                                if(t.getBounds().touches(bounds)) {
                                    for(int j = 0; j != t.getTriangleCount(); j++) {
                                        if(resolve(t.getTriangle(j, triangle), position, hNormal, rPosition)) {
                                            hNode = n;
                                        }
                                    }
                                    return true;
                                }
                                return false;
                            });
                        } else {
                            for(int j = 0; j != n.getTriangleCount(); j++) {
                                if(resolve(n.getTriangle(scene, j, triangle), position, hNormal, rPosition)) {
                                    hNode = n;
                                }
                            }
                        }
                    }
                    return true;
                }
                return false;
            });
            if(hNode != null) {
                if(Math.acos(Math.max(-0.999f, Math.min(0.999f, hNormal.dot(0, 1, 0)))) < Math.toRadians(groundSlope)) {
                    groundNormal.add(hNormal);
                    onGround = true;
                    velocity.y = 0;
                }
                if(Math.acos(Math.max(-0.999f, Math.min(0.999f, hNormal.dot(0, -1, 0)))) < Math.toRadians(roofSlope)) {
                    hitRoof = true;
                    velocity.y = 0;
                }
                position.set(rPosition);
                collided = true;
                if(collisionListener != null) {
                    collisionListener.collided(hNode, hTriangle);
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
        return collided;
    }

    private boolean resolve(Triangle triangle, Vector3f position, Vector3f hNormal, Vector3f rPosition) {
        float t = time[0];

        triangle.n.negate(d);
        o.set(position);
        if(triangle.intersectsPlane(o, d, time)) {
            p.set(d).mul(time[0]);
            o.add(p, p);
            if(triangle.contains(p, 0)) {
                hNormal.set(triangle.n);
                hTriangle.set(triangle);
                p.add(rPosition.set(hNormal).mul(radius), rPosition);
                return true;
            } else {
                time[0] = t;
                triangle.closestPoint(o, c);
                o.sub(c, d);
                if(d.length() > 0.0000001 && d.length() < time[0]) {
                    time[0] = d.length();
                    d.normalize(hNormal);
                    hTriangle.set(triangle);
                    c.add(rPosition.set(hNormal).mul(radius), rPosition);
                    return true;
                }
            }
        }
        tested++;

        return false;
    }
}
