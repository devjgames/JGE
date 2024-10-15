package org.jge.demo;


import java.util.Random;
import java.util.Vector;

import org.jge.EnumRadioButtons;
import org.jge.GFX;
import org.jge.Game;
import org.jge.Hidden;
import org.jge.MD2Mesh;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.PathNavigator;
import org.jge.PathNode;
import org.joml.Vector3f;

public class NPC extends NodeComponent {
    
    public static enum MouseMode {
        ADD,
        SELECTMOVE,
        DELETE
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
    private final Vector<PathNode> locations = new Vector<>();
    private final Random random = new Random();
    private final PathNavigator navigator = new PathNavigator();

    @Override
    public void init() throws Exception {
        root = PathNode.load(path);

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

            navigator.setStart(root);

            Node node = node().getChild(0);

            node.position.set(root.position.x, node.position.y, root.position.z);
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

        if(!navigator.hasPath()) {
            for(PathNode location : locations) {
                if(random.nextInt(2) == 0 && location != navigator.getStart()) {
                    navigator.findPath(location);
                    if(navigator.hasPath()) {
                        node.position.set(navigator.getStart().position.x, node.position.y, navigator.getStart().position.z);
                    }
                    break;
                }
            }
        }
        if(navigator.hasPath()) {
            float y = node.position.y;

            navigator.move(node.position, 25, node.rotation);
            node.position.y = y;
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
                            selection.addChild(node);
                            selection = node;
                        }
                        path = root.toString();
                        root.calcBounds();
                    }
                }
            } else if(mode == MouseMode.SELECTMOVE) {
                if(!down && root != null) {
                    selection = root.select(origin, direction, time);
                } else if(selection != null) {
                    scene().move(selection.position, -Game.getInstance().dX(), -Game.getInstance().dY());
                    root.calcBounds();
                    path = root.toString();
                }
            } else if(mode == MouseMode.DELETE) {
                if(!down && root != null) {
                    selection = root.select(origin, direction, time);
                    if(selection != null) {
                        PathNode parent = selection.getParent();

                        if(parent != null) {
                            selection.detach();;
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
}
