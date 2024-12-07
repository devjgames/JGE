package org.jge.demo.components;

import org.jge.Collider;
import org.jge.Game;
import org.jge.IO;
import org.jge.NodeComponent;
import org.jge.Resource;
import org.jge.SpriteRenderer;
import org.jge.Texture;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;


public class Player extends NodeComponent {
    
    public final Vector3f direction = new Vector3f(1, 0, 0);
    public float speed = 100;
    public float gravity = 2000;

    private boolean pDown = false;
    private final Vector3f f = new Vector3f();

    private final Collider collider = new Collider();

    public Collider getCollider() {
        return collider;
    }

    @Override
    public void init() throws Exception {

        scene().eye.set(node().position);
        scene().eye.add(direction.normalize(), scene().target);

        Game.getInstance().setContinuousMouseEnabled(true);
    }

    @Override
    public void update() throws Exception {
        Game game = Game.getInstance();

        scene().rotateAroundEye(game.getDeltaX() * -0.02f, game.getDeltaY() * -0.02f);

        if(game.isKeyDown(GLFW.GLFW_KEY_P)) {
            if(!pDown) {
                System.out.println((int)node().position.x + ", " + (int)node().position.y + ", " + (int)node().position.z);
                pDown = true;
            }
        } else {
            pDown = false;
        }

        scene().target.sub(scene().eye, f);
        direction.set(f).mul(1, 0, 1);
        collider.velocity.mul(0, 1, 0);
        if((game.isButtonDown(0) || game.isButtonDown(1)) && direction.length() > 0.0000001) {
            direction.normalize().mul(speed * ((game.isButtonDown(0)) ? 1 : -1));
            collider.velocity.add(direction);
        }
        collider.velocity.y -= gravity * game.getElapsedTime();
        collider.resolve(scene(), scene().root, scene().eye);
        scene().eye.add(f, scene().target);
        node().position.set(scene().eye);
    }

    @Override
    public void renderSprites() throws Exception {
        Texture font = Game.getInstance().assets.load(IO.file("assets/font.png"));
        SpriteRenderer renderer = Game.getInstance().getRenderer(SpriteRenderer.class);
        int scale = Game.getInstance().getScale();

        renderer.beginSprite(font);
        renderer.push(
            "FPS = " + Game.getInstance().getFrameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + Game.getInstance().sceneRenderer.getTrianglesRendered() + "\n" +
            "TST = " + collider.getTested() + "\n" +
            "POS = " + (int)node().position.x + ", " + (int)node().position.y + ", " + (int)node().position.z + "\n" +
            "ESC = Quit", 
            scale, 8, 12, 100, 5, 10 * scale, 10 * scale, 1, 1, 1, 1);
        renderer.endSprite();
    }

}
