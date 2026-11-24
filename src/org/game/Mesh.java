package org.game;

import java.util.Vector;

public class Mesh extends TaggedResource implements TriangleSelector {
    
    public final AABB bounds = new AABB();
    public final Mat4 model = new Mat4();
    public final Mat4 modelIT = new Mat4();

    private final Vector<MeshPart> parts = new Vector<>();

    public Mesh() {
    }

    public int getPartCount() {
        return parts.size();
    }

    public MeshPart getPart(int i) {
        return parts.get(i);
    }

    public void addPart(MeshPart part) {
        parts.add(part);
    }

    public void calcBoundsAndTransform(GameNode node) throws Exception {
        node.calcModel(model);

        modelIT.set(model).invert().transpose();

        calcBounds();
    }

    public void calcBounds() throws Exception {
        bounds.clear();
        for(MeshPart part : parts) {
            part.calcBounds();
            bounds.union(part.bounds);
        }
        bounds.buffer(1, 1, 1);
    }

    public boolean pick(Vec3 origin, Vec3 direction, float[] time) {
        boolean hit = false;

        if(bounds.intersectsRay(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z)) {
            for(MeshPart part : parts) {
                if(part.pick(origin, direction,time)) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    @Override
    public void select(AABB bounds, Object userData, Visitor visitor) {
        if(this.bounds.touches(bounds)) {
            for(MeshPart part : parts) {
                part.select(bounds, userData, visitor);
            }
        }
    }

    @Override
    public void destroy() {
        for(MeshPart part : parts) {
            part.destroy();
        }
        parts.clear();

        super.destroy();
    }
}
