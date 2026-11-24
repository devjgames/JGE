package org.game;

import java.util.UUID;

public class MeshPart extends TaggedResource implements TriangleSelector {

    public static final UUID TRANSPARENT_COLLIDABLE = UUID.randomUUID();

    public final VertexArrayBuffer vertexBuffer;
    public final VertexElementBuffer vertexElementBuffer;
    public final AABB bounds = new AABB();
    public final Mesh parent;

    private final AABB iBounds = new AABB();

    private Triangle triangle = new Triangle();

    public MeshPart(int vertexBufferCountAdder, int vertexElementBufferCountAdder, Mesh parent) {
        vertexBuffer = new VertexArrayBuffer(vertexBufferCountAdder);
        vertexElementBuffer = new VertexElementBuffer(vertexElementBufferCountAdder);
        this.parent = parent;
    }

    public AABB getIBounds() {
        return iBounds;
    }

    public void calcBounds() throws Exception {
        bounds.set(iBounds).transform(parent.model);
        bounds.buffer(1, 1, 1);
    }

    public boolean pick(Vec3 origin, Vec3 direction, float[] time) {
        boolean hit = false;

        if(bounds.intersectsRay(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z)) {
            for(int i = 0; i != vertexElementBuffer.getCount(); i += 3) {
                try {
                    int i1 = vertexElementBuffer.get(i + 0);
                    int i2 = vertexElementBuffer.get(i + 1);
                    int i3 = vertexElementBuffer.get(i + 2);
                    float x1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 0);
                    float y1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 1);
                    float z1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 2);
                    float x2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 0);
                    float y2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 1);
                    float z2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 2);
                    float x3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 0);
                    float y3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 1);
                    float z3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 2);

                    triangle.p1.set(x1, y1, z1).mulPosition(parent.model);
                    triangle.p2.set(x2, y2, z2).mulPosition(parent.model);
                    triangle.p3.set(x3, y3, z3).mulPosition(parent.model);
                    triangle.calcPlane();

                    if(triangle.n.dot(direction) < 0) {
                        float t = Intersection.intersectRayTriangle(origin, direction, triangle.p1, triangle.p2, triangle.p3, 0.0000001f);

                        if(t > 0.00000001 && t < time[0]) {
                            time[0] = t;
                            hit = true;
                        }
                    }
                } catch(Exception ex) {
                    Log.getInstance().put(0, ex);
                }
            }        
        }
        return hit;
    }

    @Override
    public void select(AABB bounds, Object userData, Visitor visitor) {
        if(get(TRANSPARENT_COLLIDABLE) == null) {
            if(this.bounds.touches(bounds)) {
                for(int i = 0; i != vertexElementBuffer.getCount(); i += 3) {
                    try {
                        int i1 = vertexElementBuffer.get(i + 0);
                        int i2 = vertexElementBuffer.get(i + 1);
                        int i3 = vertexElementBuffer.get(i + 2);
                        float x1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 0);
                        float y1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 1);
                        float z1 = vertexBuffer.get(i1 * BasicRenderer.VERTEX_STRIDE + 2);
                        float x2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 0);
                        float y2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 1);
                        float z2 = vertexBuffer.get(i2 * BasicRenderer.VERTEX_STRIDE + 2);
                        float x3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 0);
                        float y3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 1);
                        float z3 = vertexBuffer.get(i3 * BasicRenderer.VERTEX_STRIDE + 2);

                        triangle.p1.set(x1, y1, z1).mulPosition(parent.model);
                        triangle.p2.set(x2, y2, z2).mulPosition(parent.model);
                        triangle.p3.set(x3, y3, z3).mulPosition(parent.model);
                        triangle.calcPlane();

                        visitor.visit(triangle, userData);

                    } catch(Exception ex) {
                        Log.getInstance().put(0, ex);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
        vertexBuffer.destroy();
        vertexElementBuffer.destroy();
        super.destroy();
    }
}
