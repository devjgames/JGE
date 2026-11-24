package org.game;

public interface TriangleSelector {
    
    public static interface Visitor {
        public void visit(Triangle triangle, Object userData);
    }

    public void select(AABB bounds, Object userData, Visitor visitor);
}
