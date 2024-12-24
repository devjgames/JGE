package org.quest.components;

import java.util.Random;

import org.jge.BlendState;
import org.jge.DepthState;
import org.jge.Game;
import org.jge.IO;
import org.jge.NodeComponent;
import org.jge.Particle;
import org.jge.ParticleSystem;

public class FireLight extends NodeComponent {

    public float speed = 2;

    private final Particle particle = new Particle();
    private final Random random = new Random();

    @Override
    public void init() throws Exception {
        ParticleSystem particles = new ParticleSystem(25);

        node().renderable = particles;
        node().zOrder = 100;
        node().blendState = BlendState.ADDITIVE;
        node().depthState = DepthState.READONLY;

        particles.texture = Game.getInstance().getAssets().load(IO.file("assets/particle.png"));
    }

    @Override
    public void update() throws Exception {
        ParticleSystem particles = (ParticleSystem)node().renderable;
        float i = 0.5f * random.nextFloat() + 0.5f;
        float startS = 20 + random.nextFloat() * 40;
        float endS = 2 + random.nextFloat() * 4;

        particle.velocityX = -10 + random.nextFloat() * 20;
        particle.velocityY = -10 + random.nextFloat() * 20;
        particle.velocityZ = -10 + random.nextFloat() * 20;
        particle.endR = 0;
        particle.endG = 0;
        particle.endB = 0;
        particle.startR = i;
        particle.startG = i;
        particle.startB = i;
        particle.startX = startS;
        particle.startY = startS;
        particle.endX = endS;
        particle.endY = endS;
        particle.lifeSpan = 0.5f + random.nextFloat() * 0.5f;

        particles.emit(particle);

        particles.emitPosition.set(0,  25 * (float)Math.sin(speed * Game.getInstance().totalTime()), 0);
    }
}