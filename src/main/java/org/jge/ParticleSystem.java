package org.jge;

import java.io.File;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ParticleSystem implements Renderable {
    
      public final Vector3f emitPosition = new Vector3f();

    private float[] particles;
    private float[] temp;
    private int count;
    private final Matrix4f m = new Matrix4f();
    private final Vector3f r = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f f = new Vector3f();
    private float time = 0;
    private int stackTop = 0;
    private final int maxParticles;
    private final Vector<VertexPTC> vertices = new Vector<>();
    private final Vector<Integer> indices = new Vector<>();
    private final AABB bounds = new AABB();
    private int particleCount = 0;

    public ParticleSystem(int maxParticles) throws Exception {
        for(int i = 0; i != maxParticles * 4; i++) {
            vertices.add(new VertexPTC());
        }
        for(int i = 0; i != maxParticles * 4; i += 4) {
            indices.add(i);
            indices.add(i + 1);
            indices.add(i + 2);
            indices.add(i + 2);
            indices.add(i + 3);
            indices.add(i);
        }
        this.maxParticles = maxParticles;

        count = 0;
        particles = new float[maxParticles * 20];
        temp = new float[maxParticles * 20];
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public int getTriangleCount() {
        return particleCount * 2;
    }

    @Override
    public Triangle getTriangle(Scene scene, Node node, int i, Triangle triangle) {
        i *= 3;
        triangle.p1.set(vertices.get(indices.get(i + 0)).position);
        triangle.p2.set(vertices.get(indices.get(i + 1)).position);
        triangle.p3.set(vertices.get(indices.get(i + 2)).position);
        return triangle.calcPlane();
    }

    public AABB getBounds() {
        return bounds;
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {

        time = Game.getInstance().getTotalTime();
        int count = 0;
        for(int i = 0; i != this.count; i += 20) {
            float n = (time - particles[i + 18]) / particles[i + 19];
            if(n <= 1) {
                for(int j = 0; j != 20; j++) {
                    temp[count++] = particles[i + j];
                }
            }
        }
        float[] t = particles;
        particles = temp;
        temp = t;
        this.count = count;

        m.set(scene.view).mul(node.model).transpose();
        r.set(m.m00(), m.m01(), m.m02());
        u.set(m.m10(), m.m11(), m.m12());
        f.set(m.m20(), m.m21(), m.m22());

        bounds.clear();
        stackTop = 0;
        particleCount = 0;
        for(int i = 0; i != count; particleCount++) {
            float vX = particles[i++];
            float vY = particles[i++];
            float vZ = particles[i++];
            float pX = particles[i++];
            float pY = particles[i++];
            float pZ = particles[i++];
            float sR = particles[i++];
            float sG = particles[i++];
            float sB = particles[i++];
            float sA = particles[i++];
            float eR = particles[i++];
            float eG = particles[i++];
            float eB = particles[i++];
            float eA = particles[i++];
            float sX = particles[i++];
            float sY = particles[i++];
            float eX = particles[i++];
            float eY = particles[i++];
            float s = time - particles[i++];
            float n = s / particles[i++];
            float cR = sR + n * (eR - sR);
            float cG = sG + n * (eG - sG);
            float cB = sB + n * (eB - sB);
            float cA = sA + n * (eA - sA);
            float x = (sX + n * (eX - sX)) * 0.5f;
            float y = (sY + n * (eY - sY)) * 0.4f;

            pX += s * vX;
            pY += s * vY;
            pZ += s * vZ;
            push(pX - r.x * x - u.x * y, pY - r.y * x - u.y * y, pZ - r.z * x - u.z * y, 0, 0, f.x, f.y, f.z, cR, cG, cB, cA);
            push(pX + r.x * x - u.x * y, pY + r.y * x - u.y * y, pZ + r.z * x - u.z * y, 1, 0, f.x, f.y, f.z, cR, cG, cB, cA);
            push(pX + r.x * x + u.x * y, pY + r.y * x + u.y * y, pZ + r.z * x + u.z * y, 1, 1, f.x, f.y, f.z, cR, cG, cB, cA);
            push(pX - r.x * x + u.x * y, pY - r.y * x + u.y * y, pZ - r.z * x + u.z * y, 0, 1, f.x, f.y, f.z, cR, cG, cB, cA);
            bounds.add(pX - r.x * x - u.x * y, pY - r.y * x - u.y * y, pZ - r.z * x - u.z * y);
            bounds.add(pX + r.x * x - u.x * y, pY + r.y * x - u.y * y, pZ + r.z * x - u.z * y);
            bounds.add(pX + r.x * x + u.x * y, pY + r.y * x + u.y * y, pZ + r.z * x + u.z * y);
            bounds.add(pX - r.x * x + u.x * y, pY - r.y * x + u.y * y, pZ - r.z * x + u.z * y);
        }
    }

    private void push(float x, float y, float z, float s, float t, float nx, float ny, float nz, float r, float g, float b, float a) {
        VertexPTC v = vertices.get(stackTop++);

        v.position.set(x, y, z);
        v.textureCoordinate.set(s, t);
        v.color.set(r, g, b, a);
    }
    
    public void emit(Particle particle) {
        if(count != particles.length) {
            particles[count++] = particle.velocityX;
            particles[count++] = particle.velocityY;
            particles[count++] = particle.velocityZ;
            particles[count++] = particle.positionX + emitPosition.x;
            particles[count++] = particle.positionY + emitPosition.y;
            particles[count++] = particle.positionZ + emitPosition.z;
            particles[count++] = particle.startR;
            particles[count++] = particle.startG;
            particles[count++] = particle.startB;
            particles[count++] = particle.startA;
            particles[count++] = particle.endR;
            particles[count++] = particle.endG;
            particles[count++] = particle.endB;
            particles[count++] = particle.endA;
            particles[count++] = particle.startX;
            particles[count++] = particle.startY;
            particles[count++] = particle.endX;
            particles[count++]  = particle.endY;
            particles[count++] = Game.getInstance().getTotalTime();
            particles[count++] = particle.lifeSpan;
        }
    }

    @Override
    public int render(Scene scene, Node node, Vector<Node> lights) throws Exception {
        ColorRenderer renderer = Game.getInstance().getRenderer(ColorRenderer.class);

        renderer.begin(scene.projection, scene.view, node.model, node.texture);
        renderer.push(vertices, indices, particleCount * 6);
        renderer.end();

        return particleCount * 2;
    }

    @Override
    public Renderable newInstance(boolean deepCopy) throws Exception {
        ParticleSystem particles = new ParticleSystem(maxParticles);

        return particles;
    }
}
