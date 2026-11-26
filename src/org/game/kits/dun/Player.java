package org.game.kits.dun;

import java.io.File;

import org.game.BasicRenderer;
import org.game.CameraHelper;
import org.game.Collidable;
import org.game.Collider;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameNodeSelector;
import org.game.GameSystem;
import org.game.GameSystem2;
import org.game.PostProcess;
import org.game.IOHelper;
import org.game.Resource;
import org.game.Shader;
import org.game.SoundEngine;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.Triangle;
import org.game.TriangleSelector;
import org.game.Vec3;
import org.game.Collider.ContactHandler;

public class Player extends Model implements ContactHandler {

    public static final int COLLIDABLE_DEFAULT = 0;
    public static final int COLLIDABLE_DEATH = 1;

    private static boolean start = false;

    public static boolean started() {
        return start;
    }

    private Collider collider = new Collider();
    private Vec3 f = new Vec3();
    private Vec3 d = new Vec3();
    private SoundEngine.Sound death;
    private File loadFile = null;
    private float seconds = 0;
    private boolean dead = false;
    private PostProcess post = null;
    
    public Player() {
        super("axis.obj", false);
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        post = gs.game.getResources().manage(
            new PostProcess(new String(
                IOHelper.readAllBytes(new File(gs.game.getResources().getRoot(), "Toon.glsl"))
            ), 
            "vTexCoord"
        ));

        ((GameSystem2)gs).setPost(null);

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
                    collider.selectors.add(new GameNodeSelector(n, "mesh", collidable.getCollidableTag()));
                }
            }
        }

        gs.eye.set(node.position);
        gs.eye.add(node.r, gs.target);
        gs.up.set(node.u);

        gs.game.setMouseGrabbed(true);

        death = gs.game.getResources().load(new File("death.wav"));
        loadFile = null;
        seconds = 0;
        dead = false;

        start = false;
        
        gs.game.resetTimer();
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        GameSystem2 gs2 = (GameSystem2)gs;

        if(node.value.trim().equals("toon")) {
            gs2.setPost((texture) -> {
                Shader shader = post.begin(texture, gs.backgroundColor);

                shader.set("uColorTex", texture, 0);
                shader.set("uTexelSize", 1.0f / texture.width, 1.0f / texture.height);
                shader.set("uBands", 9);
                shader.set("uBandSoftness", 0.0f);
                shader.set("uSaturation", 1.0f);
                shader.set("uEdgeWidth", (float)gs.game.getScale());
                shader.set("uEdgeThreshold", 0.3f);
                shader.set("uOutlineColor", 0, 0, 0);

                return post.end();
            });
        } else {
            gs2.setPost(null);
        }

        if(gs.inDesign()) {
            return;
        }

        if(!start) {
            if(gs.game.isMouseButtonDown(0)) {
                start = true;
            }
            return ;
        }

        if(dead) {
            seconds += gs.game.getElapsedTime();
            if(seconds >= 1) {
                loadFile = new File(new File("kits", "dunKit"), gs.getSceneName());
            }
            return;
        }

        CameraHelper.rotateAroundEye(gs.eye, gs.target, gs.up, gs.game.getDeltaX() * 0.025f, gs.game.getDeltaY() * 0.025f);

        gs.target.sub(gs.eye, d).normalize();
        f.set(d).mul(1, 0, 1);
        collider.velocity.mul(0, 1, 0);
        if(f.length() > 0.0000001) {
            f.normalize();
            if(gs.game.isMouseButtonDown(0)) {
                f.mul(75);
            } else if(gs.game.isMouseButtonDown(1)) {
                f.mul(-75);
            } else {
                f.zero();
            };
            collider.velocity.add(f);
        }
        collider.velocity.y -= 2000 * gs.game.getElapsedTime();
        collider.resolve(gs.eye);
        gs.eye.add(d, gs.target);
    }

    @Override
    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
        Texture texture = gs.game.getResources().load(new File("sprites.png"));
        int s = gs.game.getScale();

        spriteRenderer.begin(texture);
        if(!start && !gs.inDesign()) {
            spriteRenderer.push(
                "click to start", 16, 10, 12, s * 2, 5, 
                gs.w() / 2 - 7 * 20 * s, gs.h() / 2 - 12 * s, 
                1, 1, 1, 1, 1, 1, 1, 1
            );
        } else {
            spriteRenderer.push(
                "FPS = " + gs.game.getFPS() + "\n" +
                "RES = " + Resource.getInstances() + "\n" +
                "TRI = " + basicRenderer.getTrianglesRendered() + "\n" +
                "TST = " + collider.getTested() + "\n" +
                "POS = " + (int)gs.eye.x + ", " + (int)gs.eye.y + ", " + (int)gs.eye.z + "\n",
                16, 10, 12, s, 5, 10 * s, 10 * s, 1, 1, 1, 1, 1, 1, 1, 1
            );
        }
        spriteRenderer.end();
    }

    @Override
    public void contactMade(TriangleSelector selector, Triangle triangle) {
        if(selector instanceof GameNodeSelector) {
            GameNodeSelector gns = (GameNodeSelector)selector;

            if(gns.getTag() == Player.COLLIDABLE_DEATH) {
                death.setVolume(0.5f);
                death.play(false);
                dead = true;
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
