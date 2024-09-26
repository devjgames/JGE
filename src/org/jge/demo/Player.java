package org.jge.demo;

import org.jge.Collider;
import org.jge.Game;
import org.jge.IO;
import org.jge.MD2Mesh;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Resource;
import org.jge.Sound;
import org.jge.SpriteRenderer;
import org.jge.Texture;
import org.jge.Triangle;

import org.joml.Vector2f;
import org.joml.Vector3f;


public class Player extends NodeComponent {
    
    public float length = 100;
    public float height = 25;

    private final Vector3f f = new Vector3f();
    private final Vector3f o = new Vector3f();
    private final Vector3f d = new Vector3f();
    private final float[] time = new float[1];
    private final Collider collider = new Collider();
    private final Triangle hTriangle = new Triangle();

    @Override
    public void init() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        Vector3f o = scene().calcOffset();

        float dx = o.x;
        float dz = o.z;
        float dl = Vector2f.length(dx, dz);
        float dy = height;

        scene().target.set(node().position);
        scene().target.add(dx / dl * length, dy, dz / dl * length, scene().eye);

        if(node().getChildCount() != 0) {
            Node child = node().getChild(0);
            MD2Mesh mesh;

            if(child.renderable instanceof MD2Mesh) {
                mesh = (MD2Mesh)child.renderable;

                mesh.setSequence(0, 39, 10, true);

                child.rotation
                .identity()
                .rotate((float)Math.toRadians(-90), 1, 0, 0)
                .translate(0, 0, -mesh.getBounds().min.z - collider.radius + 1);
            }
        }

        collider.collisionListener = (n, t) -> {
            if(t.tag == 256) {
                Sound sound = Game.getInstance().getAssets().load(IO.file("assets/items/collect.wav"));

                sound.setVolume(0.75f);
                sound.play(false);

                n.visible = false;
                n.collidable = false;
            }
        };
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign()) {
            return;
        }
        if(Game.getInstance().buttonDown(2)) {
            scene().rotate(Game.getInstance().dX() * 0.02f, 0);
        }

        boolean moving = false;
        float dx = scene().eye.x - scene().target.x;
        float dz = scene().eye.z - scene().target.z;
        float x = Game.getInstance().w() / 2 - Game.getInstance().mouseX();
        float y = Game.getInstance().mouseY() - Game.getInstance().h() / 2;
        float l = Vector2f.length(x, y);
        MD2Mesh mesh = null;

        if(node().getChildCount() != 0) {
            Node child = node().getChild(0);

            if(child.renderable instanceof MD2Mesh) {
                mesh = (MD2Mesh)child.renderable;
            }
        }

        collider.velocity.mul(0, 1, 0);  
        scene().target.sub(scene().eye, f);
        f.y = 0;  
        if(f.length() > 0.0000001 && l > 0.001 && Game.getInstance().buttonDown(0)) {
            scene().move(collider.velocity, -x / l * collider.speed, -y / l * collider.speed);
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

            moving = true;
        }
        if(mesh != null) {
            if(moving) {
                mesh.setSequence(40, 45, 8, true);
            } else {
                mesh.setSequence(0, 39, 10, true);
            }
        }
        collider.velocity.y -= collider.gravity * Game.getInstance().elapsedTime();
        collider.resolve(scene(), scene().root, node().position);

        l = length;
        o.set(node().position);
        d.set(dx, 0, dz);
        d.normalize();
        time[0] = l + (collider.radius - 1);
        if(collider.intersect(scene(), scene().root, o, d, 1, 0xFF, time, hTriangle)) {
            l = Math.min(time[0], length) - (collider.radius - 1);
        }
        d.mul(1, 0, 1).normalize(l);
        d.y = height + (length - l);

        l = d.length();
        d.normalize();
        time[0] = l + (collider.radius - 1);
        if(collider.intersect(scene(), scene().root, o, d, 1, 0xFF, time, hTriangle)) {
            l = Math.min(time[0], l) - (collider.radius - 1);
        }
        d.mul(l);

        scene().target.set(node().position);
        scene().target.add(d, scene().eye);
        scene().up.set(0, 1, 0);
    }

    @Override
    public void renderSprites() throws Exception {
        Texture font = Game.getInstance().getAssets().load(IO.file("assets/font.png"));
        Texture icon = Game.getInstance().getAssets().load(IO.file("assets/player_i.png"));
        SpriteRenderer renderer = Game.getInstance().getRenderer(SpriteRenderer.class);

        renderer.beginSprite(font);
        renderer.push(
            "FPS = " + Game.getInstance().frameRate() + ", RES = " + Resource.getInstances() + ", TST = " + collider.getTested() + 
            ", TRI = " + Game.getInstance().getSceneRenderer().getTrianglesRendered(), 8, 12, 100, 5, 10, 10, 1, 1, 1, 1);
        renderer.endSprite();
        renderer.beginSprite(icon);
        renderer.push(0, 0, icon.w, icon.h, 10, Game.getInstance().h() - 10 - icon.h * 2, icon.w * 2, icon.h * 2, 1, 1, 1, 1);
        renderer.endSprite();
    }

}
