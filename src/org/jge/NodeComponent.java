package org.jge;

import java.io.File;

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

    public void handleInput() throws Exception {
    }

    public int render() throws Exception {
        return 0;
    }

    public void renderSprites() throws Exception {
    }

    public File loadFile() {
        return null;
    }

    public void remove() {
        node.components.remove(this);
    }

    final void newInstance(Scene scene, Node node) throws Exception {
        NodeComponent component = (NodeComponent)getClass().getConstructors()[0].newInstance();

        Utils.copy(this, component);

        component.init(scene, node);

        node.components.add(component);
    }
}
