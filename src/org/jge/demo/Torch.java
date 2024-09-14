package org.jge.demo;

import java.util.Random;

import org.jge.BlendState;
import org.jge.DepthState;
import org.jge.Game;
import org.jge.IO;
import org.jge.NodeComponent;
import org.jge.Particle;
import org.jge.ParticleSystem;

public class Torch extends NodeComponent {

    private Particle particle = new Particle();
    private Random random = new Random(100000);
    
    @Override
    public void init() throws Exception {
        ParticleSystem particles = new ParticleSystem(100);

        node().texture = Game.getInstance().getAssets().load(IO.file("assets/particle.png"));
        node().renderable = particles;

        node().depthState = DepthState.READONLY;
        node().blendState = BlendState.ADDITIVE;

        node().zOrder = 100;
    }

    @Override
    public void update() throws Exception {
        ParticleSystem particles = (ParticleSystem)node().renderable;

        for(int i = 0; i != 2; i++) {
        
            particle.velocityY = 10 + random.nextFloat() * 20;
            particle.positionX = -5 + random.nextFloat() * 5;
            particle.positionY = -5 + random.nextFloat() * 5;
            particle.positionZ = -5 + random.nextFloat() * 5;
            particle.startA = 1;
            particle.startR = particle.startG = particle.startB = 0.25f + random.nextFloat() * 0.25f;
            particle.endA = 1;
            particle.endR = particle.endG = particle.endB = 0;
            particle.startX = particle.startY = 5 + random.nextFloat() * 20;
            particle.endX = particle.endY = 2 + random.nextFloat() * 2;
            particle.lifeSpan = 0.5f + random.nextFloat();

            particles.emit(particle);
        }
    }
}
