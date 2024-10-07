package org.jge;

import java.util.Iterator;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Node implements Iterable<Node> {

    public static interface Visitor {
        boolean visit(Node node) throws Exception;
    }
    
    public String name = "Node";
    @Hidden
    public String data = "";
    @MultiLine
    public String text = "";
    public boolean visible = true;
    public boolean collidable = false;
    public boolean dynamic = false;
    public boolean isLight = false;
    public boolean isSpotLight = false;
    public boolean receivesShadow = true;
    public boolean castsShadow = true;
    public final Vector3f position = new Vector3f();
    public final Vector3f absolutePosition = new Vector3f();
    public final Matrix4f rotation = new Matrix4f();
    public final Vector3f scale = new Vector3f(1, 1, 1);
    public final AABB bounds = new AABB();
    public final Matrix4f localModel = new Matrix4f();
    public final Matrix4f model = new Matrix4f();
    public final Matrix4f modelIT = new Matrix4f();
    public final Vector4f ambientColor = new Vector4f(0.15f, 0.15f, 0.15f, 1);
    public final Vector4f diffuseColor = new Vector4f(1, 1, 1, 1);
    public final Vector4f lightColor = new Vector4f(1, 1, 1, 1);
    public float lightShadowOffset = 5;
    public float lightSpotCutOffDegrees = 45;
    public Texture texture = null;
    public Texture decal = null;
    public float lightRadius = 300;
    public DepthState depthState = DepthState.READWRITE;
    public CullState cullState = CullState.BACK;
    public BlendState blendState = BlendState.OPAQUE;
    public int zOrder = 0;
    public int minTrisPerTree = 16;
    public int triangleTag = 1;
    public Renderable renderable = null;

    RenderTarget lightShadowMap = null;

    private final Vector<Node> children = new Vector<>();
    private Node parent = null;
    private final Vector3f r = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f f = new Vector3f();
    private final Matrix4f m = new Matrix4f();
    private OctTree octTree = null;

    final Vector<NodeComponent> components = new Vector<>();

    public Node() {
    }

    public Node(Scene scene, Node node) throws Exception {
        if(node.renderable != null) {
            renderable = node.renderable.newInstance();
        }
        if(node.texture != null) {
            texture = Game.getInstance().getAssets().load(node.texture.file);
        }
        Utils.copy(node, this);
        for(Node child : node) {
            addChild(new Node(scene, child));
        }
        for(NodeComponent component : node.components) {
            component.newInstance(scene, this);
        }
    }

    public int getComponentCount() {
        return components.size();
    }

    public NodeComponent getComponent(int i) {
        return components.get(i);
    }

    public void addComponent(Scene scene, NodeComponent component) {
        component.init(scene, this);
        components.add(component);
    }

    public void clearComponents() {
        components.clear();
    }

     public int getTriangleCount() {
        if(renderable != null) {
            return renderable.getTriangleCount();
        }
        return 0;
    }

    public Triangle getTriangle(Scene scene, int i, Triangle triangle) {
        if(renderable != null) {
            return renderable.getTriangle(scene, this, i, triangle).transform(model).setTag(triangleTag);
        }
        return null;
    }

    public OctTree getOctTree(Scene scene) {
        if(octTree == null && !dynamic && renderable != null) {
            Vector<Triangle> triangles = new Vector<>();

            for(int i = 0; i != getTriangleCount(); i++) {
                triangles.add(getTriangle(scene, i, new Triangle()));
            }
            octTree = OctTree.create(triangles, minTrisPerTree);
        }
        return octTree;
    }

    public void clearOctTree() {
        octTree = null;
    }

    public int getChildCount() {
        return children.size();
    }

    public Node getChild(int i) {
        return children.get(i);
    }
    
    public Node getRoot() {
        Node root = this;

        while(root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public Node getParent() {
        return parent;
    }

    public void detach() {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public void addChild(Node node) {
        node.detach();
        node.parent = this;
        children.add(node);
    }

    public void detachAllChildren() {
        while(!children.isEmpty()) {
            children.firstElement().detach();
        }
    }

    public Matrix4f calcLightProjection(Matrix4f matrix) {
        return matrix.identity().perspective((float)Math.toRadians(90), 1, 1, lightRadius);
    }

    public Matrix4f calcLightView(Matrix4f matrix) {
        return matrix.identity().lookAt(
            absolutePosition.x, absolutePosition.y, absolutePosition.z,
            absolutePosition.x - rotation.m10(), 
            absolutePosition.y - rotation.m11(),
            absolutePosition.z - rotation.m12(),
            rotation.m00(), rotation.m01(), rotation.m02()
        );
    }

    public void calcBoundsAndTransform() {
        localModel
            .identity()
            .translate(position)
            .mul(rotation)
            .scale(scale);
        model.set(localModel);
        if(parent != null) {
            model.set(parent.model).mul(localModel);
        }
        model.invert(modelIT).transpose();
        absolutePosition.zero().mulPosition(model);
        bounds.clear();
        if(renderable != null) {
            bounds.set(renderable.getBounds()).transform(model);
        }
        if(isLight) {
            Vector3f p = absolutePosition;
            float r = lightRadius;

            bounds.add(p.x - r, p.y - r, p.z - r);
            bounds.add(p.x + r, p.y + r, p.z + r);
        }
        for(Node node : this) {
            node.calcBoundsAndTransform();
            bounds.add(node.bounds);
        }
    }

    public void rotate(int axis, float amount) {
        r.set(rotation.m00(), rotation.m01(), rotation.m02());
        u.set(rotation.m10(), rotation.m11(), rotation.m12());
        f.set(rotation.m20(), rotation.m21(), rotation.m22());

        if(axis == 0) {
            m.identity().rotate(amount, r);
            u.mulDirection(m);
            f.mulDirection(m);
        } else if(axis == 1) {
            m.identity().rotate(amount, u);
            r.mulDirection(m);
            f.mulDirection(m);
        } else {
            m.identity().rotate(amount, f);
            r.mulDirection(m);
            u.mulDirection(m);
        }
        rotation.set(
            r.x, r.y, r.z, 0,
            u.x, u.y, u.z, 0,
            f.x, f.y, f.z, 0,
            0, 0, 0, 1
        );
    }

    public void traverse(Visitor v) throws Exception {
        if(v.visit(this)) {
            for(Node node : this) {
                node.traverse(v);
            }
        }
    }

    public Node find(Visitor v) throws Exception {
        if(v.visit(this)) {
            return this;
        }
        for(Node node : this) {
            Node r = node.find(v);

            if(r != null) {
                return r;
            }
        }
        return null;
    }

    public void init() throws Exception {
        for(NodeComponent component : components) {
            if(!component.setup()) {
                component.init();
            }
        }
    }

    public void start() throws Exception {
        for(NodeComponent component : components) {
            if(!component.setup()) {
                component.start();
                component.complete();
            }
        }
    }

    public void update(Scene scene) throws Exception {
        for(NodeComponent component : components) {
            component.update();
        }
        if(renderable != null) {
            renderable.update(scene, this);
        }
    }

    public void renderSprites() throws Exception {
        for(NodeComponent component : components) {
            component.renderSprites();;   
        }
    }

    public void handleInput(Scene scene) throws Exception {
        for(NodeComponent component : components) {
            component.handleInput();
        }
    }

    public void initAndStart() throws Exception {
        traverse((n) -> {
            n.init();
            return true;
        });
        traverse((n) -> {
            n.start();
            return true;
        });
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }
}
