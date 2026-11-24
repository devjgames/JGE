package org.game;

import java.util.Hashtable;
import java.util.Vector;

public class GameNode {
    
    public String name = "";
    public final Vec3 position = new Vec3();
    public final Vec3 r = new Vec3(1, 0, 0);
    public final Vec3 u = new Vec3(0, 1, 0);
    public final Vec3 f = new Vec3(0, 0, 1);
    public final Vec3 scale = new Vec3(1, 1, 1);
    public final Vec3 absolutePosition = new Vec3();
    public final Hashtable<String, Object> data = new Hashtable<>();
    public String value = "";

    private Mat4 m = new Mat4();
    private Vector<GameNode> children = new Vector<>();
    private GameNode parent = null;

    public void rotate(int axis, float degrees) {
        if(axis == 0) {
            m.identity().rotate((float)Math.toRadians(degrees), r);
            u.mulDirection(m).normalize();
            f.mulDirection(m).normalize();
        } else if(axis == 1) {
            m.identity().rotate((float)Math.toRadians(degrees), u);
            r.mulDirection(m).normalize();
            f.mulDirection(m).normalize();
        } else if(axis == 2) {
            m.identity().rotate((float)Math.toRadians(degrees), f);
            u.mulDirection(m).normalize();
            r.mulDirection(m).normalize();
        }
    }

    public GameNode getParent() {
        return parent;
    }

    public GameNode getRoot() {
        GameNode root = this;

        while(root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public int getChildCount() {
        return children.size();
    }

    public GameNode getChild(int i) {
        return children.get(i);
    }

    public void addChild(GameNode node) {
        node.parent = this;
        children.add(node);
    }

    public GameNode find(String name) {
        if(this.name.equals(name)) {
            return this;
        }
        for(GameNode node : children) {
            GameNode r = node.find(name);

            if(r != null) {
                return r;
            }
        }
        return null;
    }

    public void calcModel(Mat4 model) {
        doCalcModel((Mat4)model.identity());

        absolutePosition.zero().mulPosition(model);
    }

    private void doCalcModel(Mat4 model) {
        if(parent != null) {
            parent.doCalcModel(model);
        }
        model
            .translate(position)
            .mul(
                r.x, r.y, r.z, 0,
                u.x, u.y, u.z, 0,
                f.x, f.y, f.z, 0,
                0, 0, 0, 1
            )
            .scale(scale);
    }
}
