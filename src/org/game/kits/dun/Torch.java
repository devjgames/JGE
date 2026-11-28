package org.game.kits.dun;

import java.io.File;
import java.util.Random;

import org.game.AABB;
import org.game.BasicRenderer;
import org.game.BasicRenderer2;
import org.game.BlendState;
import org.game.GFX;
import org.game.GameComponent;
import org.game.GameLight;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Mat4;
import org.game.Particle;
import org.game.ParticleSystem;
import org.game.RenderPass;
import org.game.Vec3;
import org.game.Vec4;

public class Torch extends GameComponent {

    private Random random = new Random(100);
    private Particle particle = new Particle();
    private Vec4 color;
    private Mat4 model = new Mat4();

    public Torch(float r, float g, float b, float a) {
        color = new Vec4(r, g, b, a);
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        if(i == 0) {
            GameLight light = (GameLight)node.data.get("gameLight");

            if(light == null) {
                light = new GameLight();

                light.color.set(color);
                node.data.put("gameLight", light);
            }

            light.radius = (Float)node.data.get("radius");

            node.calcModel(model);

            light.position.set(0, 0, 0).mulPosition(model);

            return light;
        }
        return null;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        ParticleSystem ps;

        if(!node.data.containsKey("radius")) {
            float r = 50;

            node.data.put("radius", r);
            node.value = "" + r;
        }

        node.data.put("fire", gs.game.getResources().manage(ps = new ParticleSystem(500)));
        node.data.put("acm", 0.0f);

        ps.texture = gs.game.getResources().load(new File("fire.png"));
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        ParticleSystem ps = (ParticleSystem)node.data.get("fire");

        node.calcModel(model);

        return ps.pick(origin, direction, model, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        float acm = (Float)node.data.get("acm");
        ParticleSystem ps = (ParticleSystem)node.data.get("fire");

        acm += gs.game.getElapsedTime();
        while(acm >= 0) {
            float startColor = 0.5f + random.nextFloat() * 0.5f;
            float startSize = 10 + random.nextFloat() * 20;
            
            particle.velocityY = 10 + random.nextFloat() * 20;
            particle.positionX = -2 + random.nextFloat() * 4;
            particle.positionY = -2 + random.nextFloat() * 4;
            particle.positionZ = -2 + random.nextFloat() * 4;
            particle.startR = startColor;
            particle.startG = startColor;
            particle.startB = startColor;
            particle.startA = 1;
            particle.endR = 0;
            particle.endG = 0;
            particle.endB = 0;
            particle.endA = 1;
            particle.startX = startSize;
            particle.startY = startSize;
            particle.endX = 1;
            particle.endY = 0;
            particle.lifeSpan = 0.5f + random.nextFloat() * 1.5f;

            ps.emit(particle, gs.game);

            acm -= 1 / 120.0f;
        }
        node.data.put("acm", acm);

        node.calcModel(model);

        ps.update(gs.game, gs.view, model);
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer basicRenderer, RenderPass renderPass) throws Exception {
        if(renderPass != RenderPass.TRANSPARENT) {
            return;
        }

        ParticleSystem ps = (ParticleSystem)node.data.get("fire");

        node.calcModel(model);

        if(gs.frustum.testAab((AABB)ps.getBounds().transform(model))) {
            GFX.setBlendState(BlendState.ADDITIVE);

            basicRenderer.setLightingEnabled(false);
            basicRenderer.setTexture(ps.texture);
            basicRenderer.setModel(model);

            BasicRenderer2 br2 = (BasicRenderer2)basicRenderer;
            
            br2.setDecal(null);
            br2.setWarp(false, 0, 0, 0, false);

            basicRenderer.draw(ps.vertexBuffer);
        }
    }

    @Override
    public void parseValue(GameSystem gs, GameNode node) throws Exception {
        try {
            node.data.put("radius", Float.parseFloat(node.value.trim()));
        } catch(NumberFormatException ex) {

        }
    }

    @Override
    public String toString() {
        return "torch";
    }
}
