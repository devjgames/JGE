package org.quest.components;

import org.jge.Collider;
import org.jge.Game;
import org.jge.IO;
import org.jge.Node;
import org.jge.NodeComponent;
import org.jge.Resource;
import org.jge.SpriteRenderer;
import org.jge.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;

public class Player extends NodeComponent {

    public float speed = 100;
    public float radius = 16;
    public float fall = -600;

    private final Collider collider = new Collider();
    private final Vector3f offset = new Vector3f();
    private File loadFile = null;

    @Override
    public void init() throws Exception {
        offset.set(scene().calcOffset());

        collider.radius = radius;
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        Game game = Game.getInstance();

        if(game.buttonDown(2)) {
            scene().rotate(game.dX() * 0.025f, 0);
            offset.set(scene().calcOffset());
        }

        float dx = game.mouseX() - game.w() / 2;
        float dy = game.mouseY() - game.h() / 2;
        float dl = Vector2f.length(dx, dy);

        collider.velocity.mul(0, 1, 0);
        if(game.buttonDown(0)) {
            scene().move(collider.velocity, dx / dl * speed, -dy / dl * speed);

            float len = Vector2f.length(collider.velocity.x, collider.velocity.z);
            float x = collider.velocity.x / len;
            float z = collider.velocity.z / len;
            float radians = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, x)));

            if(z > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            node().rotation.identity().rotate(radians, 0, 1, 0);

            Node child = node().getChild(0);

            child.rotation.rotate((float)Math.toRadians(-180 * game.elapsedTime()), 0, 0, 1);
        }
        collider.velocity.y -= 2000 * game.elapsedTime();
        collider.resolve(scene(), scene().root, node().position);

        scene().target.set(node().position);
        scene().target.y = 0;
        scene().target.add(offset, scene().eye);

        if(node().position.y < fall) {
            loadFile = scene().file;
        }
    }

    @Override
    public File loadFile() {
        return loadFile;
    }

    @Override
    public void renderSprites() throws Exception {
        Game game = Game.getInstance();
        Texture font = game.getAssets().load(IO.file("assets/font.png"));
        SpriteRenderer renderer = game.getRenderer(SpriteRenderer.class);

        renderer.beginSprite(font);
        renderer.push(
            "FPS = " + game.frameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + game.getSceneRenderer().getTrianglesRendered() + "\n" + 
            "TST = " + collider.getTested(),
            8, 12, 100, 5, 10, 10, 1, 1, 1, 1);
        renderer.endSprite();
    }
}
