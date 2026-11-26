package org.game;

public class GameNodeSelector implements TriangleSelector {

    private GameNode node;
    private String meshName;
    private int tag;
    private boolean enabled;

    public GameNodeSelector(GameNode node, String meshName, int tag) {
        this.node = node;
        this.tag = tag;
        this.meshName = meshName;
        this.enabled = true;
    }

    public GameNode getNode() {
        return node;
    }

    public int getTag() {
        return tag;
    }

    public String getMeshName() {
        return meshName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void select(AABB bounds, Object userData, Visitor visitor) {
        if(enabled) {
            try {
                Object obj = node.getRoot().data.get(meshName);

                if(obj instanceof KeyFrameMesh) {
                    KeyFrameMesh mesh = (KeyFrameMesh)obj;

                    mesh.getMesh().calcBoundsAndTransform(node);
                    mesh.getMesh().select(bounds, userData, visitor);
                } else if(obj instanceof Mesh) {
                    Mesh mesh = (Mesh)obj;

                    mesh.calcBoundsAndTransform(node);
                    mesh.select(bounds, userData, visitor);
                }
            } catch(Exception ex) {
                Log.getInstance().put(0, ex);
            }
        }
    }

}