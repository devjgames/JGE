package org.jge.demo;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.jge.AABB;
import org.jge.EnumRadioButtons;
import org.jge.GFX;
import org.jge.Game;
import org.jge.Hidden;
import org.jge.LineRenderer;
import org.jge.MD2Mesh;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Renderable;
import org.jge.Scene;
import org.jge.Triangle;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class NPC extends NodeComponent {
    
    public static enum MouseMode {
        ADD,
        SELECTMOVE,
        DELETE
    }

    public static class PathNode implements Renderable {

        public final Vector3f position = new Vector3f();
        public final Vector<PathNode> children = new Vector<>();
        public PathNode parent = null;
        
        private final AABB bounds = new AABB();

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
    }

    @Hidden
    public String path="";
    @EnumRadioButtons
    public MouseMode mode = MouseMode.ADD;

    private PathNode root = null;
    private PathNode selection = null;
    private boolean down = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final AABB bounds = new AABB();
    private final Vector<PathNode> locations = new Vector<>();
    private final Vector<PathNode> locationPath = new Vector<>();
    private final HashSet<PathNode> visited = new HashSet<>();
    private final Random random = new Random();
    private PathNode start = null;

    @Override
    public void init() throws Exception {
        String[] lines = path.split("\\n+");
        Vector<PathNode> stack = new Vector<>();

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

        if(root == null) {
            root = (PathNode)node().renderable;
        }

        if(root != null) {
            root.calcBounds();
        }
        selection = null;

        if(scene().isInDesign()) {
            node().renderable = root;
        } else if(root != null && node().getChildCount() != 0) {
            locations.clear();
            root.getLocations(locations);

            start = locations.get(0);

            Node node = node().getChild(0);

            node.position.set(start.position.x, node.position.y, start.position.z);
        }
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign() || root == null || node().getChildCount() == 0) {
            return;
        }

        Node node = node().getChild(0);
        MD2Mesh mesh = (MD2Mesh)node.renderable;

        mesh.setSequence(40, 45, 7, true);

        if(locationPath.isEmpty()) {
            for(PathNode location : locations) {
                if(random.nextInt(2) == 0 && location != start) {
                    visited.clear();

                    start.getLocationPath(locationPath, location, visited);
                    start = locationPath.firstElement();

                    System.out.println("path = " + locationPath.size());

                    node.position.set(start.position.x, node.position.y, start.position.z);
                    break;
                }
            }
        }
        if(locationPath.size() > 1) {
            PathNode start = locationPath.firstElement();
            PathNode next = locationPath.get(1);

            float dx = next.position.x - start.position.x;
            float dz = next.position.z - start.position.z;
            float l = Vector2f.length(dx, dz);
            float nx = dx / l;
            float nz = dz / l;
            float px = node.position.x + nx * 25 * Game.getInstance().elapsedTime();
            float pz = node.position.z + nz * 25 * Game.getInstance().elapsedTime();
            float tx = px - start.position.x;
            float tz = pz - start.position.z;
            float s = tx * nx + tz * nz;

            if(s >= Vector2f.length(dx, dz)) {
                px = next.position.x;
                pz = next.position.z;
                locationPath.remove(0);
                if(locationPath.size() == 1) {
                    this.start = locationPath.get(0);
                    locationPath.clear();
                }
            }
            node.position.set(px, node.position.y, pz);

            float radians = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, nx)));

            if(nz > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            node.rotation.identity().rotate((float)Math.toRadians(-90), 1, 0, 0).rotate(radians, 0, 0, 1);
        } 
    }

    @Override
    public void handleInput() throws Exception {
        if(Game.getInstance().buttonDown(0)) {
            int w = Game.getInstance().w();
            int h = Game.getInstance().h();
            int x = Game.getInstance().mouseX();
            int y = h - Game.getInstance().mouseY() - 1;

            GFX.unproject(x, y, 0, 0, 0, w, h, scene().projection, scene().view, origin);
            GFX.unproject(x, y, 1, 0, 0, w, h, scene().projection, scene().view, direction);

            time[0] = Float.MAX_VALUE;

            direction.sub(origin).normalize();

            if(mode == MouseMode.ADD) {
                if(!down) {
                    float t = direction.dot(0, 1, 0);

                    if(Math.abs(t) >= 0.0000001) {
                        t = -origin.dot(0, 1, 0) / t;
                        
                        origin.add(direction.mul(t, point), point);

                        if(root == null) {
                            root = new PathNode();
                            root.position.set(point).add(0, 8, 0);
                            selection = root;
                            node().renderable = root;
                        } else if(selection != null) {
                            PathNode node = new PathNode();

                            node.position.set(point).add(0, 8, 0);
                            node.parent = selection;
                            selection.children.add(node);
                            selection = node;
                        }
                        path = root.toString();
                        root.calcBounds();
                    }
                }
            } else if(mode == MouseMode.SELECTMOVE) {
                if(!down && root != null) {
                    select(root);
                } else if(selection != null) {
                    scene().move(selection.position, -Game.getInstance().dX(), -Game.getInstance().dY());
                    root.calcBounds();
                    path = root.toString();
                }
            } else if(mode == MouseMode.DELETE) {
                if(!down && root != null) {
                    select(root);
                    if(selection != null) {
                        PathNode parent = selection.parent;

                        if(parent != null) {
                            parent.children.remove(selection);
                            selection = null;
                            path = root.toString();
                            root.calcBounds();
                        } else {
                            path = "";
                            root = null;
                            selection = null;
                            node().renderable = null;
                        }
                    }
                }
            }
            down = true;
        } else {
            down = false;
        }
    }

    private void select(PathNode node) {
        bounds.clear();
        bounds.add(node.position);
        bounds.buffer(8, 8, 8);
        if(bounds.isects(origin, direction, time)) {
            selection = node;
        }
        for(PathNode child : node.children) {
            select(child);
        }
    }
}
