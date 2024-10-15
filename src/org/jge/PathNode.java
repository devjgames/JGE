package org.jge;

import java.io.File;
import java.util.HashSet;
import java.util.Vector;

import org.joml.Vector3f;

public class PathNode implements Renderable {
    
    public final Vector3f position = new Vector3f();

    private final Vector<PathNode> children = new Vector<>();
    private PathNode parent;
    private final AABB bounds = new AABB();
    private final AABB pBounds = new AABB();

    @Override
    public File getFile() {
        return null;
    }
    @Override
    public AABB getBounds() {
        return bounds;
    }

    @Override
    public int getTriangleCount() {
        return 0;
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        return triangle;
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
    }

    @Override
    public int renderShadowPass(Scene scene, Node node, Node light) throws Exception {
        return 0;
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        LineRenderer renderer = Game.getInstance().getRenderer(LineRenderer.class);

        renderer.begin(scene.projection, scene.view, node.model);
        render(renderer);
        renderer.end();

        return 0;
    }

    private void render(LineRenderer renderer) {
        renderer.push(position.x, position.y, position.z, 1, 0, 0, 1, position.x + 8, position.y, position.z, 1, 0, 0, 1);
        renderer.push(position.x, position.y, position.z, 1, 0, 0, 1, position.x, position.y + 8, position.z, 1, 0, 0, 1);
        renderer.push(position.x, position.y, position.z, 1, 0, 0, 1, position.x, position.y, position.z + 8, 1, 0, 0, 1);
        for(PathNode child : children) {
            renderer.push(position.x, position.y, position.z, 1, 1, 1, 1, child.position.x, child.position.y, child.position.z, 1, 1, 1, 1);
            child.render(renderer);
        }
    }

    @Override
    public Renderable newInstance() throws Exception {
        return this;
    }

    public PathNode getParent() {
        return parent;
    }

    public PathNode getRoot() {
        PathNode root = this;

        while(root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    public int getChildCount() {
        return children.size();
    }

    public PathNode getChild(int i) {
        return children.get(i);
    }

    public void detach() {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public void addChild(PathNode child) {
        child.detach();
        child.parent = this;
        children.add(child);
    }

    public PathNode select(Vector3f origin, Vector3f direction, float[] time) {
        pBounds.clear();
        pBounds.add(position);
        pBounds.buffer(8, 8, 8);
        if(pBounds.isects(origin, direction, time)) {
            return this;
        }
        for(PathNode child : children) {
            PathNode r = child.select(origin, direction, time);

            if(r != null) {
                return r;
            }
        }
        return null;
    }

    public void calcBounds() {
        bounds.clear();
        bounds.add(position);
        for(PathNode child : children) {
            child.calcBounds();;
            bounds.add(child.bounds);
        }
        bounds.buffer(16, 16, 16);
    }

    public void getLocations(Vector<PathNode> locations) {
        if(locations.isEmpty() || children.isEmpty()) {
            locations.add(this);
        }
        for(PathNode child : children) {
            child.getLocations(locations);
        }
    }

    public boolean getLocationPath(Vector<PathNode> path, PathNode location, HashSet<PathNode> visited) {
        if(visited.contains(this)) {
            return false;
        }
        visited.add(this);
        path.add(this);
        if(location == this) {
            return true;
        } else if(parent != null) {
            if(parent.getLocationPath(path, location, visited)) {
                return true;
            }
        }
        for(PathNode child : children) {
            if(child.getLocationPath(path, location, visited)) {
                return true;
            }
        }
        path.remove(path.size() - 1);
        
        return false;
    }

    @Override
    public String toString() {
        String text = "node " + position.x + " " + position.y + " " + position.z + "\n";

        for(PathNode child : children) {
            text += child.toString();
        }
        text += "end\n";

        return text;
    }

    public static PathNode load(String path) {
        String[] lines = path.split("\\n+");
        Vector<PathNode> stack = new Vector<>();
        PathNode root = null;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("node ")) {
                PathNode node = new PathNode();

                node.position.set(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                if(!stack.isEmpty()) {
                    node.parent = stack.lastElement();
                    stack.lastElement().children.add(node);
                }
                stack.add(node);
            } else if(tLine.startsWith("end")) {
                root = stack.remove(stack.size() - 1);
            }
        }
        return root;
    }
}
