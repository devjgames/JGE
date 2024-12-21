package org.jge.demo;

import org.jge.Collider;
import org.jge.Game;
import org.jge.IO;
import org.jge.NodeComponent;
import org.jge.Resource;
import org.jge.SpriteRenderer;
import org.jge.Texture;
import org.jge.Triangle;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.event.KeyEvent;


public class Player extends NodeComponent {
    
    public float length = 200;
    public float speed = 100;
    public float gravity = 2000;
    public int jump = 0;

    private final Vector3f f = new Vector3f();
    private final Vector3f o = new Vector3f();
    private final Vector3f d = new Vector3f();
    private final Triangle hTriangle = new Triangle();
    private final Collider collider = new Collider();
    private final float[] time = new float[1];
    private boolean down = false;

    public Collider getCollider() {
        return collider;
    }

    @Override
    public void init() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        Vector3f o = scene().calcOffset();

        o.normalize(length);

        scene().target.set(node().position);
        scene().target.add(o, scene().eye);
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        if(Game.getInstance().keyDown(KeyEvent.VK_S)) {
            if(!down) {
                d.set(-node().rotation.m00(), 0.2f, -node().rotation.m02()).normalize().normalize(length);
                scene().target.add(d, scene().eye);
                scene().up.set(0, 1, 0);
                down = true;
            }
        } else {
            down = false;
        }

        if(Game.getInstance().keyDown(KeyEvent.VK_SPACE) && collider.isOnGround() && jump > 0) {
            collider.velocity.y = jump;
        }

        if(Game.getInstance().buttonDown(2)) {
            scene().rotate(Game.getInstance().dX() * 0.025f, Game.getInstance().dY() * 0.025f);
        }

        float x = Game.getInstance().w() / 2 - Game.getInstance().mouseX();
        float y = Game.getInstance().mouseY() - Game.getInstance().h() / 2;
        float l = Vector2f.length(x, y);

        collider.velocity.mul(0, 1, 0);  
        scene().target.sub(scene().eye, f);
        f.y = 0;  
        if(f.length() > 0.0000001 && l > 0.001 && Game.getInstance().buttonDown(0)) {
            scene().move(collider.velocity, -x / l * speed, -y / l * speed);
            x = collider.velocity.x;
            y = collider.velocity.z;
            l = Vector2f.length(x, y);
            x /= l;
            y /= l;

            float radians = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, x)));

            if(y > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            node().rotation.identity().rotate(radians, 0, 1, 0);
            node().getChild(0).rotate(2, (float)Math.toRadians(-360 * Game.getInstance().elapsedTime()));
        }
        collider.velocity.y -= gravity * Game.getInstance().elapsedTime();
        collider.resolve(scene(), scene().root, node().position);

        o.set(node().position);
        d.set(scene().calcOffset()).normalize();
        time[0] = length + 8;
        if(collider.intersect(scene(), scene().root, o, d, 1, 0xF, time, false, hTriangle)) {
            d.mul(time[0] - 8);
        } else {
            d.mul(length);
        }

        scene().target.set(o);
        scene().target.add(d, scene().eye);
    }

    @Override
    public void renderSprites() throws Exception {
        Texture font = Game.getInstance().getAssets().load(IO.file("assets/font.png"));
        SpriteRenderer renderer = Game.getInstance().getRenderer(SpriteRenderer.class);

        renderer.beginSprite(font);
        renderer.push(
            "FPS = " + Game.getInstance().frameRate() + ", RES = " + Resource.getInstances() + ", TRI = " + Game.getInstance().getSceneRenderer().getTrianglesRendered() + 
            ", TESTED = " + collider.getTested(), 8, 12, 100, 5, 10, 10, 1, 1, 1, 1);
        renderer.endSprite();
    }

}
