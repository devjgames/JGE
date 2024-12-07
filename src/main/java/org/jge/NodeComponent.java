package org.jge;

public abstract class NodeComponent {

    private Scene scene = null;
    private Node node = null;
    private boolean setup = false;

    public final Scene scene() {
        return scene;
    }

    public final Node node() {
        return node;
    }

    final boolean setup() {
        return setup;
    }

    final void complete() {
        setup = true;
    }

    final void init(Scene scene, Node node) {
        this.scene = scene;
        this.node = node;
    }

    public void init() throws Exception {
    }

    public void start() throws Exception {
    }

    public void update() throws Exception {
    }
    
    public void renderSprites() throws Exception {
    }
}
