package org.jge;

import java.util.Vector;

import org.joml.Vector3f;

public class OctTree {

    public static interface Visitor {
        
        boolean visit(OctTree tree) throws Exception;
    }

    public static OctTree create(Vector<Triangle> triangles, int minTrisPerTree) {
        AABB bounds = new AABB();

        for(Triangle triangle : triangles) {
            bounds.add(triangle.p1);
            bounds.add(triangle.p2);
            bounds.add(triangle.p3);
        }
        return new OctTree(triangles, bounds, minTrisPerTree);
    }
    
    private final AABB bounds = new AABB();
    private final Vector<OctTree> children = new Vector<>();
    private final Vector<Triangle> triangles = new Vector<>();

    @SuppressWarnings("unchecked")
    public OctTree(Vector<Triangle> triangles, AABB bounds, int minTrisPerTree) {
        this.bounds.set(bounds);
        if(triangles.size() < minTrisPerTree) {
            this.triangles.addAll(triangles);
        } else {
            Vector3f c = bounds.center(new Vector3f());
            Vector3f l = bounds.min;
            Vector3f h = bounds.max;
            AABB[] ba = new AABB[] {
                new AABB(l.x, l.y, l.z, c.x, c.y, c.z),
                new AABB(c.x, l.y, l.z, h.x, c.y, c.z),
                new AABB(c.x, c.y, l.z, h.x, h.y, c.z),
                new AABB(c.x, c.y, c.z, h.x, h.y, h.z),
                new AABB(l.x, c.y, l.z, c.x, h.y, c.z),
                new AABB(l.x, c.y, c.z, c.x, h.y, h.z),
                new AABB(l.x, l.y, c.z, c.x, c.y, h.z),
                new AABB(c.x, l.y, c.z, h.x, c.y, h.z)
            };
            Vector<Triangle>[] list = new Vector[8];
            Vector<Triangle> keep = new Vector<>();

            for(int i = 0; i != list.length; i++) {
                list[i] = new Vector<>();
            }
            for(Triangle triangle : triangles) {
                boolean contained = false;

                for(int i = 0; i != list.length; i++) {
                    if(
                        ba[i].contains(triangle.p1) && 
                        ba[i].contains(triangle.p2) &&
                        ba[i].contains(triangle.p3)
                    ) {
                        contained = true;
                        list[i].add(triangle);
                        break;
                    }
                }
                if(!contained) {
                    keep.add(triangle);
                }
            }
            this.triangles.addAll(keep);
            for(int i = 0; i != list.length; i++) {
                if(!list[i].isEmpty()) {
                    children.add(new OctTree(list[i], ba[i], minTrisPerTree));
                }
            }
        }
    }

    public AABB getBounds() {
        return bounds;
    }

    public int getTriangleCount() {
        return triangles.size();
    }

    public Triangle getTriangle(int i, Triangle triangle) {
        return triangle.set(triangles.get(i));
    }

    public int getChildCount() {
        return children.size();
    }

    public OctTree getChild(int i) {
        return children.get(i);
    }

    public void traverse(Visitor v) throws Exception {
        if(v.visit(this)) {
            for(OctTree tree : children) {
                tree.traverse(v);
            }
        }
    }
}
