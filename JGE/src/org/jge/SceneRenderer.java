package org.jge;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.jge.Mesh.MeshPart;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL2;

public class SceneRenderer {
    
    private final Vector<Node> renderables = new Vector<>();
    private final Vector<Node> lights = new Vector<>();
    private final Vector<Node> meshes = new Vector<>();
    private final Vector<Triangle> triangles = new Vector<>();
    private int trianglesRendered = 0;
    private Node axis = new Node();
    private final Matrix4f matrix = new Matrix4f();

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public void render(Scene scene) throws Exception {

        renderables.clear();
        lights.clear();
        meshes.clear();
        triangles.clear();

        scene.calcBoundsAndTransform();
        scene.root.initAndStart();
        scene.calcBoundsAndTransform();
        scene.root.traverse((n) -> {
            n.update(scene);
            return true;
        });
        scene.calcBoundsAndTransform();

        scene.root.traverse((n) -> {
            if(n.visible) {
                if(scene.frustum.testAab(n.bounds.min, n.bounds.max)) {
                    if(n.isLight) {
                        lights.add(n);
                    }
                    return true;
                }
            }
            return false;
        });

        lights.sort((a, b) -> {
            float da = a.absolutePosition.distance(scene.eye);
            float db = b.absolutePosition.distance(scene.eye);

            return Float.compare(da, db);
        });

        trianglesRendered = 0;

        scene.root.traverse((n) -> {
            if(n.visible) {
                if(scene.frustum.testAab(n.bounds.min, n.bounds.max)) {
                    if(n.renderable != null) {
                        renderables.add(n);
                    } else if(n.isLight) {
                        if(scene.isInDesign()) {
                            renderables.add(n);
                        }
                    }
                    return true;
                }
            }
            return false;
        });

        if(scene.isInDesign()) {
            renderables.add(axis);
        }

        boolean rebuild = scene.rebuildLightMap;
        File file = IO.file(scene.file.getParentFile(), IO.getFilenameWithoutExtension(scene.file) + ".png");

        if(!rebuild) {
            rebuild = !file.exists();
        }

        if(scene.calcLightMap || rebuild) {
            try {
                int w = scene.lightMapWidth;
                int h = scene.lightMapHeight;
                int samples = scene.samples;
                float sradius = scene.sampleRadius;
                int[] pixels = null;
                OctTree octTree = null;
                int x = 0;
                int y = 0;
                int mh = 0;
                Random random = new Random(100000);

                scene.root.traverse((n) -> {
                    if(n.visible) {
                        if(scene.frustum.testAab(n.bounds.min, n.bounds.max)) {
                            if(n.renderable != null) {
                                if(n.renderable instanceof Mesh) {
                                    if(n.lightMapEnabled) {
                                        meshes.add(n);
                                    }
                                    if(n.castsShadow) {
                                        for(int i = 0; i != n.getTriangleCount(); i++) {
                                            Triangle triangle = n.getTriangle(scene, i, new Triangle());
        
                                            triangles.add(triangle);
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                    }
                    return false;
                });
                if(rebuild) {
                    octTree = OctTree.create(triangles, 16);
                    pixels = new int[w * h];
                    for(int i = 0; i != pixels.length; i++) {
                        pixels[i] = 0xFFFF00FF;
                    }
                }
                for(Node node : meshes) {
                    Mesh mesh = (Mesh)node.renderable;

                    for(MeshPart part : mesh.parts) {
                        for(int[] polygon : part.polygons) {
                            VertexPT2N v1 =  part.vertices.get(polygon[0]);
                            VertexPT2N v2 =  part.vertices.get(polygon[1]);
                            VertexPT2N v3 =  part.vertices.get(polygon[2]);
                            Vector3f p1 = new Vector3f(v1.position).mulPosition(node.model);
                            Vector3f p2 = new Vector3f(v2.position).mulPosition(node.model);
                            Vector3f p3 = new Vector3f(v3.position).mulPosition(node.model);
                            Vector3f e1 = p2.sub(p1, new Vector3f()).normalize();
                            Vector3f normal = v1.normal.normalize(new Vector3f());
                            Vector3f e2 = normal.cross(e1, new Vector3f()).normalize();
                            float minS = Float.MAX_VALUE;
                            float minT = Float.MAX_VALUE;
                            float maxS = -Float.MAX_VALUE;
                            float maxT = -Float.MAX_VALUE;

                            for(int i : polygon) {
                                VertexPT2N v = part.vertices.get(i);
                                Vector3f p = new Vector3f(v.position).mulPosition(node.model);
                                float s = p.dot(e1);
                                float t = p.dot(e2);

                                minS = Math.min(s, minS);
                                minT = Math.min(t, minT);
                                maxS = Math.max(s, maxS);
                                maxT = Math.max(t, maxT);
                            }

                            minS = (float)Math.floor(minS / 16) * 16;
                            minT = (float)Math.floor(minT / 16) * 16;
                            maxS = (float)Math.ceil(maxS / 16) * 16;
                            maxT = (float)Math.ceil(maxT / 16) * 16;

                            int tw = (int)(maxS - minS) / 16 + 1;
                            int th = (int)(maxT - minT) / 16 + 1;

                            if(x + tw >= w) {
                                if(mh == 0 || y + mh >= h) {
                                    throw new Exception("light map overflow");
                                }
                                x = 0;
                                y = y + mh;
                                mh = 0;
                            }
                            if(x + tw >= w || y + mh >= h) {
                                throw new Exception("light map overflow");
                            }
                            mh = Math.max(th, mh);

                            for(int i : polygon) {
                                VertexPT2N v = part.vertices.get(i);
                                Vector3f p = new Vector3f(v.position).mulPosition(node.model);
                                float s = p.dot(e1);
                                float t = p.dot(e2);

                                s -= minS;
                                s = (float)Math.floor(s / 16) * 16 + 8;
                                s /= 16;
                                s = (s + x) / (float)w;

                                t -= minT;
                                t = (float)Math.floor(t / 16) * 16 + 8;
                                t /= 16;
                                t = (t + y) / (float)h;

                                v.textureCoordinate2.set(s, t);
                            }

                            boolean isPar = polygon.length == 4 && scene.lightMapParellelograms;

                            if(isPar) {
                                VertexPT2N v4 = part.vertices.get(polygon[3]);
                                Vector3f p4 = new Vector3f(v4.position).mulPosition(node.model);
                                float a1, a2, a3, a4;
                                Vector3f d1 = new Vector3f();
                                Vector3f d2 = new Vector3f();
                                
                                p2.sub(p1, d1).normalize();
                                p4.sub(p1, d2).normalize();
                                a1 = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, d1.dot(d2))));

                                p2.sub(p3, d1).normalize();
                                p4.sub(p3, d2).normalize();
                                a2 = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, d1.dot(d2))));

                                p1.sub(p2, d1).normalize();
                                p3.sub(p2, d2).normalize();
                                a3 = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, d1.dot(d2))));

                                p1.sub(p4, d1).normalize();
                                p3.sub(p4, d2).normalize();
                                a4 = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, d1.dot(d2))));

                                isPar = Math.abs(a1 - a2) < 0.1f && Math.abs(a3 - a4) < 0.1f;
                            }

                            if(isPar) {
                                VertexPT2N v4 = part.vertices.get(polygon[3]);

                                v1.textureCoordinate2.x = ((float)x + 0.5f) / (float)w;
                                v1.textureCoordinate2.y = ((float)y + 0.5f) / (float)h;
                                v2.textureCoordinate2.x = ((float)(x + tw) - 0.5f) / (float)w;
                                v2.textureCoordinate2.y = ((float)y + 0.5f) / (float)h;
                                v3.textureCoordinate2.x = ((float)(x + tw) - 0.5f) / (float)w;
                                v3.textureCoordinate2.y = ((float)(y + th) - 0.5f) / (float)h;
                                v4.textureCoordinate2.x = ((float)x + 0.5f) / (float)w;
                                v4.textureCoordinate2.y = ((float)(y + th) - 0.5f) / (float)h;

                                if(rebuild) {
                                    System.out.println("is parellelogram");
                                }
                            }

                            Vector2f t1 = part.vertices.get(polygon[0]).textureCoordinate2;
                            Vector2f t2 = part.vertices.get(polygon[1]).textureCoordinate2;
                            Vector2f t3 = part.vertices.get(polygon[2]).textureCoordinate2;
                            Vector2f u0 = t2.sub(t1, new Vector2f());
                            Vector2f u1 = t3.sub(t1, new Vector2f());

                            int hitCount = 0;

                            if(rebuild) {
                                System.out.println(x + ", " + y + " -> " + tw + " x " + th);

                                for(int i = x; i < x + tw; i++) {
                                    for(int j = y; j < y + th; j++) {
                                        float tx, ty;
                                        Vector3f p = new Vector3f();

                                        if(isPar) {
                                            VertexPT2N v4 = part.vertices.get(polygon[3]);
                                            Vector3f p4 = new Vector3f(v4.position).mulPosition(node.model);

                                            tx = ((float)(i - x) + 0.5f) / (float)tw;
                                            ty = ((float)(j - y) + 0.5f) / (float)th;

                                            Vector3f a = p1.lerp(p2, tx, new Vector3f());
                                            Vector3f b = p4.lerp(p3, tx, new Vector3f());
                                            a.lerp(b, ty, p);
                                        } else {
                                            tx = ((float)i + 0.5f) / (float)w;
                                            ty = ((float)j + 0.5f) / (float)h;

                                            Vector2f u2 = new Vector2f(tx, ty).sub(t1);
                                            float d00 = u0.dot(u0);
                                            float d01 = u0.dot(u1);
                                            float d11 = u1.dot(u1);
                                            float d20 = u2.dot(u0);
                                            float d21 = u2.dot(u1);
                                            float det = (d00 * d11 - d01 * d01);
                                            float by = (d11 * d20 - d01 * d21) / det;
                                            float bz = (d00 * d21 - d01 * d20) / det;
                                            float bx = 1.0f - by - bz;
                                            p.set(
                                                bx * p1.x + by * p2.x + bz * p3.x,
                                                bx * p1.y + by * p2.y + bz * p3.y,
                                                bx * p1.z + by * p2.z + bz * p3.z
                                            );
                                        }
                                        Vector4f c = new Vector4f(0, 0, 0, 1);

                                        for(Node light : lights) {
                                            Vector3f lightOffset = light.absolutePosition.sub(p, new Vector3f());
                                            Vector3f lightNormal = lightOffset.normalize(new Vector3f());
                                            float lDotN = Math.min(Math.max(lightNormal.dot(normal), 0), 1);
                                            float atten = 1 - Math.min(Math.max(lightOffset.length() / light.lightRadius, 0), 1);

                                            if(atten > 0 && lDotN > 0) {
                                                float s = 1;

                                                if(node.receivesShadow) {
                                                    s = 0;

                                                    for(int k = 0; k < samples; k++) {
                                                        float sx = random.nextFloat() * 2 - 1;
                                                        float sy = random.nextFloat() * 2 - 1;
                                                        float sz = random.nextFloat() * 2 - 1;

                                                        if(Vector3f.length(sx, sy, sz) < 0.0000001) {
                                                            sx = 0;
                                                            sy = 1;
                                                            sz = 0;
                                                        }
                                                        
                                                        Vector3f sample = new Vector3f(sx, sy,  sz).normalize(sradius);
                                                        Vector3f origin = p.add(normal, new Vector3f());
                                                        Vector3f direction = light.absolutePosition.add(sample, new Vector3f()).sub(origin);
                                                        float[] time = new float[] { direction.length() };
                                                        AABB bounds = new AABB();
                                                        boolean[] isects = new boolean[] { false };

                                                        bounds.add(origin);
                                                        bounds.add(origin.add(direction, new Vector3f()));
                                                        bounds.buffer(1, 1, 1);

                                                        direction.normalize();

                                                        octTree.traverse((t) -> {
                                                            if(t.getBounds().touches(bounds)) {
                                                                for(int l = 0; l != t.getTriangleCount(); l++) {
                                                                    if(t.getTriangle(l, new Triangle()).intersects(origin, direction, 0, time)) {
                                                                        isects[0] = true;
                                                                        break;
                                                                    }
                                                                }
                                                                return !isects[0];
                                                            } 
                                                            return false;
                                                        });

                                                        if(!isects[0]) {
                                                            s += 1 / (float)samples;
                                                        } else {
                                                            hitCount++;
                                                        }
                                                    }
                                                }
                                                c.add(node.diffuseColor.mul(light.lightColor, new Vector4f()).mul(lDotN * atten * s));
                                            }
                                        }
                                        c.add(node.ambientColor);

                                        float m = Math.max(c.x, Math.max(c.y, c.z));

                                        if(m > 1) {
                                            c.div(m);
                                        }

                                        int pi = j * w + i;
                                        int cr = (int)Math.min(Math.max(c.x * 255, 0), 255);
                                        int cg = (int)Math.min(Math.max(c.y * 255, 0), 255);
                                        int cb = (int)Math.min(Math.max(c.z * 255, 0), 255);

                                        pixels[pi] = 0xFF000000 | ((cr << 16) & 0xFF0000) | ((cg << 8) & 0xFF00) | (cb & 0xFF);
                                    }
                                }
                            }

                            if(rebuild) {
                                System.out.println(hitCount + " ray hits");
                            }

                            x += tw;
                        }
                    }
                }

                if(rebuild) {
                    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                    image.setRGB(0, 0, w, h, pixels, 0, w);
                    
                    ImageIO.write(image, "PNG", file);
                }

                Game.getInstance().getAssets().unload(file);

                Texture texture = Game.getInstance().getAssets().load(file);
                GL2 gl = Game.getGL();

                gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.id);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
                gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);

                BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

                ImageIO.write(image, "PNG", file);
            } finally {
                scene.calcLightMap = false;
                scene.rebuildLightMap = false;
            }
        }

        renderables.sort((a, b) -> {
            if(a.zOrder == b.zOrder) {
                float da = a.absolutePosition.distance(scene.eye);
                float db = b.absolutePosition.distance(scene.eye);

                return Float.compare(db, da);
            } else {
                return Integer.compare(a.zOrder, b.zOrder);
            }
        });

        GFX.clear(scene.backgroundColor.x, scene.backgroundColor.y, scene.backgroundColor.z, scene.backgroundColor.w);

        DepthState depthState = null;
        CullState cullState = null;
        BlendState blendState = null;

        LineRenderer lineRenderer = Game.getInstance().getRenderer(LineRenderer.class);

        for(Node node : renderables) {
            if(depthState != node.depthState) {
                GFX.setDepthState(depthState = node.depthState);
            }
            if(cullState != node.cullState) {
                GFX.setCullState(cullState = node.cullState);
            }
            if(blendState != node.blendState) {
                GFX.setBlendState(blendState = node.blendState);
            }
            if(node.renderable != null) {
                trianglesRendered += node.renderable.render(scene, node, lights);
            } else if(node.isLight) {
                lineRenderer.begin(scene.projection, scene.view, matrix.identity().translate(node.absolutePosition).mul(node.rotation));
                lineRenderer.push(0, 0, 0, 1, 0, 0, 1, 8, 0, 0, 1, 0, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 1, 0, 1, 0, 8, 0, 0, 1, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 0, 1, 1, 0, 0, 8, 0, 0, 1, 1);
                lineRenderer.end();
            } else if(node == axis) {
                float l = scene.calcOffset().length() / 6;

                lineRenderer.begin(scene.projection, scene.view, matrix.identity().translate(scene.target));
                lineRenderer.push(0, 0, 0, 1, 0, 0, 1, l, 0, 0, 1, 0, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 1, 0, 1, 0, l, 0, 0, 1, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 0, 1, 1, 0, 0, l, 0, 0, 1, 1);
                lineRenderer.end();
            }
        }

        SpriteRenderer spriteRenderer = Game.getInstance().getRenderer(SpriteRenderer.class);

        spriteRenderer.begin();
        scene.root.traverse((n) -> {
            if(n.visible) {
                n.renderSprites();
                return true;
            }
            return false;
        });
        spriteRenderer.end();

        scene.root.traverse((n) -> {
            for(int i = 0; i != n.getComponentCount(); i++) {
                File f = n.getComponent(i).loadFile();

                if(f != null) {
                    scene.loadFile = f;
                }
            }
            return true;
        });

        renderables.clear();
        lights.clear();
        meshes.clear();
        triangles.clear();
    }
}
