package org.jge;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.jge.Mesh.MeshPart;
import org.joml.Matrix4f;
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
                            if(polygon.length == 4) {
                                VertexPT2N v1 =  part.vertices.get(polygon[0]);
                                VertexPT2N v2 =  part.vertices.get(polygon[1]);
                                VertexPT2N v3 =  part.vertices.get(polygon[2]);
                                VertexPT2N v4 =  part.vertices.get(polygon[3]);
                                Vector3f p1 = new Vector3f(v1.position).mulPosition(node.model);
                                Vector3f p2 = new Vector3f(v2.position).mulPosition(node.model);
                                Vector3f p3 = new Vector3f(v3.position).mulPosition(node.model);
                                Vector3f p4 = new Vector3f(v4.position).mulPosition(node.model);
                                Vector3f e1 = p2.sub(p1, new Vector3f());
                                Vector3f e2 = p4.sub(p2, new Vector3f());
                                int tw = (int)(e1.length() / 16);
                                int th = (int)(e2.length() / 16);

                                tw = Math.max(tw, 1);
                                th = Math.max(th, 1);
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

                                v1.textureCoordinate2.x = ((float)x + 0.5f) / (float)w;
                                v1.textureCoordinate2.y = ((float)y + 0.5f) / (float)h;
                                v2.textureCoordinate2.x = ((float)(x + tw) - 0.5f) / (float)w;
                                v2.textureCoordinate2.y = ((float)y + 0.5f) / (float)h;
                                v3.textureCoordinate2.x = ((float)(x + tw) - 0.5f) / (float)w;
                                v3.textureCoordinate2.y = ((float)(y + th) - 0.5f) / (float)h;
                                v4.textureCoordinate2.x = ((float)x + 0.5f) / (float)w;
                                v4.textureCoordinate2.y = ((float)(y + th) - 0.5f) / (float)h;

                                Vector3f n = e1.cross(e2).normalize(new Vector3f());

                                int hitCount = 0;

                                if(rebuild) {
                                    System.out.println(x + ", " + y + " -> " + tw + " x " + th);

                                    for(int i = x; i < x + tw; i++) {
                                        for(int j = y; j < y + th; j++) {
                                            float tx = ((float)(i - x) + 0.5f) / (float)tw;
                                            float ty = ((float)(j - y) + 0.5f) / (float)th;
                                            Vector3f a = p1.lerp(p2, tx, new Vector3f());
                                            Vector3f b = p4.lerp(p3, tx, new Vector3f());
                                            Vector3f p = a.lerp(b, ty, new Vector3f());
                                            Vector4f c = new Vector4f(node.ambientColor);

                                            for(Node light : lights) {
                                                Vector3f lightOffset = light.absolutePosition.sub(p, new Vector3f());
                                                Vector3f lightNormal = lightOffset.normalize(new Vector3f());
                                                float lDotN = Math.min(Math.max(lightNormal.dot(n), 0), 1);
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
                                                            Vector3f origin = p.add(n, new Vector3f());
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
                            } else {
                                System.out.println("not quad geometry");
                                for(int i : polygon) {
                                    VertexPT2N v = part.vertices.get(i);

                                    v.textureCoordinate2.set(0, 0);
                                }
                            }
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
