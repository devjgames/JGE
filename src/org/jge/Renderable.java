package org.jge;

import java.io.File;
import java.util.Vector;

public interface Renderable {

    File getFile();

    AABB getBounds();

    int getTriangleCount();

    Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle);

    void update(Scene scene, Node node);

    int render(Scene scene, Node node, Vector<Node> lights);
    
    Renderable newInstance() throws Exception;
}
