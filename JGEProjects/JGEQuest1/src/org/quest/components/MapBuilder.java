package org.quest.components;

import org.jge.BlendState;
import org.jge.DepthState;
import org.jge.EnumRadioButtons;
import org.jge.GFX;
import org.jge.Game;
import org.jge.IO;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.demo.Torch;
import org.joml.Vector3f;

public class MapBuilder extends NodeComponent {

    public static enum Part {
        FLOOR,
        WALL,
        PILLAR,
        DOOR,
        TORCH_ORANGE,
        TORCH_BLUE
    }

    @EnumRadioButtons
    public Part part = Part.FLOOR;
    public boolean delete = false;

    private Node parts;
    private Node cursor;
    private float seconds = 2;
    private boolean down1 = false;
    private boolean down2 = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private float degrees = 0;

    @Override
    public void start() throws Exception {
        if(node().getChildCount() != 2) {
            node().detachAllChildren();
            node().addChild(new Node());
            node().addChild(new Node());
        }
        parts = node().getChild(0);
        cursor = node().getChild(1);
        cursor.detachAllChildren();

        parts.name = "parts";
        cursor.name = "cursor";
        cursor.detachAllChildren();
    }

    @Override
    public void update() throws Exception {
        if(!scene().isInDesign()) {
            return;
        }
        Game game = Game.getInstance();

        calcPoint();

        cursor.detachAllChildren();
        if(seconds > 1) {
            return;
        }

        seconds += game.elapsedTime();
        
        cursor.name = part.toString();
        if(part == Part.FLOOR) {
            Node node = new Node();

            point.x = (float)Math.floor(point.x / 128) * 128;
            point.z = (float)Math.floor(point.z / 128) * 128;
            node.renderable = game.getAssets().load(IO.file("assets/floor.obj"));
            node.collidable = true;
            node.rotation.identity().rotate((float)Math.toRadians(degrees), 0, 1, 0);

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(node);
        } else if(part == Part.WALL) {
            Node node = new Node();

            point.x = (float)Math.floor(point.x / 64) * 64;
            point.z = (float)Math.floor(point.z / 64) * 64;
            node.renderable = game.getAssets().load(IO.file("assets/wall.obj"));
            node.collidable = true;
            node.rotation.identity().rotate((float)Math.toRadians(degrees), 0, 1, 0);

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(node);
        } else if(part == Part.PILLAR) {
            Node node = new Node();

            point.x = (float)Math.floor(point.x / 64) * 64;
            point.z = (float)Math.floor(point.z / 64) * 64;
            node.renderable = game.getAssets().load(IO.file("assets/pillar.obj"));
            node.collidable = true;

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(node);
        } else if(part == Part.DOOR) {
            Node root = new Node();
            Node node1 = new Node();
            Node node2 = new Node();

            point.x = (float)Math.floor(point.x / 64) * 64;
            point.z = (float)Math.floor(point.z / 64) * 64;
            node1.renderable = game.getAssets().load(IO.file("assets/pillars.obj"));
            node1.collidable = true;
            node2.renderable = game.getAssets().load(IO.file("assets/door.obj"));
            node2.collidable = true;
            node2.addComponent(scene(), new Door());
            
            root.addChild(node1);
            root.addChild(node2);
            root.rotation.identity().rotate((float)Math.toRadians(degrees), 0, 1, 0);
            root.initAndStart();

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(root);
        } else if(part == Part.TORCH_ORANGE) {
            Node node = new Node();
            Node child = new Node();

            point.x = (float)Math.floor(point.x / 32) * 32;
            point.z = (float)Math.floor(point.z / 32) * 32;
            point.y = 35;
            node.addComponent(scene(), new Torch());
            child.isLight = true;
            child.position.y = 100;
            child.lightColor.set(1.5f, 0.75f, 0.25f, 1);
            node.addChild(child);
            node.initAndStart();

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(node);
        } else if(part == Part.TORCH_BLUE) {
            Node node = new Node();
            Node child = new Node();

            point.x = (float)Math.floor(point.x / 32) * 32;
            point.z = (float)Math.floor(point.z / 32) * 32;
            point.y = 35;
            node.addComponent(scene(), new Torch());
            child.isLight = true;
            child.position.y = 100;
            child.lightColor.set(0.25f, 0.75f, 1.5f, 1);
            node.addChild(child);
            node.initAndStart();

            cursor.position.set(point);
            cursor.detachAllChildren();
            cursor.addChild(node);
        } 
        cursor.traverse((n) -> {
            if(delete) {
                n.depthState = DepthState.READONLY;
                n.blendState = BlendState.ADDITIVE;
                n.zOrder = 1000;
            } else {
                n.depthState = DepthState.READWRITE;
                n.blendState = BlendState.OPAQUE;
                n.zOrder = 0;
            }
            return true;
        });
        cursor.name += "_" + (int)point.x + "_" + (int)point.z;
        hide();
    }

    @Override
    public void handleInput() throws Exception {
        Game game = Game.getInstance();

        seconds = 0;

        calcPoint();

        if(game.buttonDown(0)) {
            if(!down1) {
                down1 = true;
                delete();
                if(!delete) {
                    parts.addChild(new Node(scene(), cursor));
                }
                scene().refreshSceneTree();
            }
        } else {
            down1 = false;
        }
        if(game.buttonDown(2)) {
            if(!down2) {
                down2 = true;
                degrees += 90;
                if(degrees >= 360) {
                    degrees = 0;
                }
            }
        } else {
            down2 = false;
        }
    }

    private void calcPoint() {
        Game game = Game.getInstance();
        int w = game.w();
        int h = game.h();
        int x = game.mouseX();
        int y = h - game.mouseY() - 1;

        GFX.unproject(x, y, 0, 0, 0, w, h, scene().projection, scene().view, origin);
        GFX.unproject(x, y, 1, 0, 0, w, h, scene().projection, scene().view, direction);

        direction.sub(origin).normalize();

        float t = direction.dot(0, 1, 0);

        if(Math.abs(t) > 0.0000001) {
            t = -origin.dot(0, 1, 0) / t;
            if(t >= 0) {
                point.set(origin).add(direction.mul(t));
            }
        }
    }

    private void delete() {
        Node hit = null;

        for(Node node : parts) {
            if(node.name.equals(cursor.name)) {
                hit = node;
                break;
            }
        }
        if(hit != null) {
            hit.detach();
        }
    }

    private void hide() {
        for(Node node : parts) {
            node.visible = !(node.name.equals(cursor.name) && delete);
        }
    }
}