package org.jge;

import java.io.File;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Scene {
    
    public final Vector4f backgroundColor = new Vector4f(0.2f, 0.2f, 0.2f, 1);
    public final Vector3f eye = new Vector3f(100, 100, 100);
    public final Vector3f target = new Vector3f(0, 0, 0);
    public final Vector3f up = new Vector3f(0, 1, 0);
    public final Vector4f lightMapColor = new Vector4f(1, 1, 1, 1);
    public float fovDegrees = 60;
    public float zNear = 1;
    public float zFar = 25000;
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public final FrustumIntersection frustum = new FrustumIntersection();
    public final Node root = new Node();
    public final File file;
    public int snap = 1;
    public float sampleRadius = 32;
    public int samples = 64;
    public int lightMapWidth = 128;
    public int lightMapHeight = 128;
    public boolean lightMapParellelograms = true;
    public final Button buildLightMap = new Button() {
        @Override
        public void onClick() {
            rebuildLightMap = true;
        }
    };

    boolean rebuildLightMap = false;
    boolean calcLightMap = true;
    boolean refreshSceneTree = false;

    private final Matrix4f m = new Matrix4f();
    private final Vector3f f = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f d = new Vector3f();
    private final Vector3f o = new Vector3f();
    private final boolean inDesign;
    
    File loadFile = null;

    public Scene(File file, boolean inDesign) {
        this.file = file;
        this.inDesign = inDesign;
    }

    public boolean isInDesign() {
        return inDesign;
    }

    public Vector3f calcOffset() {
        return eye.sub(target, o);
    }

    public File getLoadFile() {
        return loadFile;
    }

    public void refreshSceneTree() {
        refreshSceneTree = true;
    }

    public void calcBoundsAndTransform() {
        projection.identity().perspective((float)Math.toRadians(fovDegrees), Game.getInstance().aspectRatio(), zNear, zFar);
        this.view.identity().lookAt(eye, target, up);
        frustum.set(m.set(projection).mul(this.view));
        root.calcBoundsAndTransform();
    }

    public void rotate(float dx, float dy) {
        eye.sub(target, f);
        m.identity().rotate(dx, 0, 1, 0);
        f.cross(up, r).mulDirection(m).normalize();
        f.mulDirection(m);
        m.identity().rotate(dy, r);
        r.cross(f, up).mulDirection(m).normalize();
        f.mulDirection(m);
        target.add(f, eye);
    }

    public void move(Vector3f point, float dx, float dy) {
        move(point, dx, dy, null);
    }

    public void move(Vector3f point, float dx, float dy, Matrix4f transform) {
        float dl = Vector2f.length(dx, dy);

        eye.sub(target, f);
        d.set(f);
        f.negate();
        f.y = 0;
        if(f.length() > 0.0000001 && dl > 0.0001) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize();
            f.mul(dy).add(r.mul(dx));
            if(transform != null) {
                f.mulDirection(transform);
            }
            point.add(f);
        }
        target.add(d, eye);
    }

    public void move(Vector3f point, float dy) {
        move(point, dy, null);
    }

    public void move(Vector3f point, float dy, Matrix4f transform) {
        eye.sub(target, d);
        u.set(0, dy, 0);
        if(transform != null) {
            u.mulDirection(transform);
        }
        point.add(u);
        target.add(d, eye);
    }

    public void zoom(float amount) {
        eye.sub(target, d);
        d.normalize(d.length() + amount);
        target.add(d, eye);
    }

    public static Scene next(Scene scene) {
        File f = scene.getLoadFile();
        Game game = Game.getInstance();

        if(f != null) {
            try {
                scene = null;
                game.getAssets().clear();
                scene = SceneSerializer.deserialize(false, f);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
        return scene;
    }
}
