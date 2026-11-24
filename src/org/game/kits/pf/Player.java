package org.game.kits.pf;

import java.io.File;

import org.game.BasicRenderer;
import org.game.CameraHelper;
import org.game.Collidable;
import org.game.Collider;
import org.game.Game;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameNodeSelector;
import org.game.GameSystem;
import org.game.Keys;
import org.game.Log;
import org.game.Mesh;
import org.game.Resource;
import org.game.SoundEngine;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.Triangle;
import org.game.TriangleSelector;
import org.game.Vec3;
import org.game.Collider.ContactHandler;

public class Player extends Model implements ContactHandler {

    private Collider collider = new Collider();
    private Vec3 d = new Vec3();
    private float seconds;
    private boolean dead;
    private File loadFile;
    
    public Player() {
        super("player.obj");
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        if(gs.inDesign()) {
            return;
        }

        collider.selectors.clear();
        collider.handler = this;

        for(int i = 0; i != gs.getCount(); i++) {
            GameComponent component = gs.getComponent(i);
            GameNode n = gs.getNode(i);

            if(component instanceof Collidable) {
                Collidable collidable = (Collidable)component;

                if(collidable.isCollidable()) {
                    collider.selectors.add( new GameNodeSelector(n.getChild(0), "mesh", collidable.getCollidableTag()));
                }
            }
        }

        d.set(node.r).add(node.u).mul(100);

        gs.target.set(node.position);
        gs.target.add(d, gs.eye);
        gs.up.set(0, 1, 0);

        seconds = 0;
        dead = false;
        loadFile = null;
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign()) {
            return;
        }

        if(dead) {
            seconds += gs.game.getElapsedTime();
            if(seconds >= 1) {
                loadFile = new File(gs.game.getResources().getRoot(), gs.getSceneName());
            }
            return ;
        }

        if(gs.game.isKeyDown(Keys.KEY_SPACE) && collider.isOnGround()) {
            collider.velocity.y = 800;
        }

        if(gs.game.isMouseButtonDown(1)) {
            CameraHelper.rotateAroundTarget(gs.eye, gs.target, gs.up, gs.game.getDeltaX() * 0.025f, 0);
        }

        float dx = gs.game.getMouseX() - gs.game.getWindowWidth() / 2;
        float dy = gs.game.getWindowHeight() / 2 - gs.game.getMouseY();
        float dl = Vec3.length(dx, 0, dy);

        collider.velocity.mul(0, 1, 0);
        if(gs.game.isMouseButtonDown(0) && dl > 0.001) {
            CameraHelper.move(collider.velocity, gs.eye, gs.target, dx / dl * 100, dy / dl * 100);
        }
        collider.velocity.y -= 2000 * gs.game.getElapsedTime();
        collider.resolve(node.position);

        gs.eye.sub(gs.target, d);
        gs.target.set(node.position);
        gs.target.add(d, gs.eye);

        if(gs.target.y < -200) {
            SoundEngine.Sound sound = gs.game.getResources().load(new File("death.wav"));

            sound.setVolume(0.25f);
            sound.play(false);
            dead = true;
        }
    }

    @Override
    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
        Texture texture = gs.game.getResources().load(new File("sprites-fx.png"));
        int s = gs.game.getScale();

        spriteRenderer.begin(texture);
        spriteRenderer.push(
            "FPS = " + gs.game.getFPS() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + basicRenderer.getTrianglesRendered() + "\n" +
            "TST = " + collider.getTested() + "\n" +
            "POS = " + (int)gs.target.x + ", " + (int)gs.target.y + ", " + (int)gs.target.z + "\n",
            16, 10, 12, s, 5, 10 * s, 10 * s, 1, 1, 0, 1, 1, 0.5f, 0, 1
        );
        spriteRenderer.end();
    }

    @Override
    public void contactMade(TriangleSelector selector, Triangle triangle) {
        if(selector instanceof GameNodeSelector) {
            GameNodeSelector gns = (GameNodeSelector)selector;
            Object o = gns.getNode().getRoot().data.get(gns.getMeshName());

            if(o instanceof Mesh) {
                try {
                    if(o == Game.getInstance().getResources().load(new File("gem.obj"))) {
                        gns.setEnabled(false);
                        gns.getNode().getRoot().data.put("visible", false);
                    }
                } catch(Exception ex) {
                    Log.getInstance().put(0, ex);
                }
            }
        }
    }

    @Override
    public File loadFile(GameSystem gs, GameNode node) throws Exception {
        return loadFile;
    }

    @Override
    public String toString() {
        return "player";
    }
}
