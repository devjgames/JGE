package org.jge;

import java.io.File;
import java.util.Vector;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import com.jogamp.opengl.GL2;

public class SceneRenderer {
    
    private Vector<Node> renderables = new Vector<>();
    private Vector<Node> lights = new Vector<>();
    private int trianglesRendered = 0;
    private Node axis = new Node();
    private Matrix4f matrix = new Matrix4f();
    private Matrix4f lightMatrix = new Matrix4f();
    private Vector<RenderTarget> shadowTargets = new Vector<>();
    private FrustumIntersection lightFrustum = new FrustumIntersection();
    private RenderTarget scaleTarget = null;

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public void render(Scene scene) throws Exception {
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

        for(int i = 0, si = 0; i != Math.min(LightRenderer.MAX_LIGHTS, lights.size()); i++) {
            Node l = lights.get(i);

            if(l.isSpotLight) {
                if(si >= shadowTargets.size()) {
                    System.out.println("allocating shadow render target ...");
                    shadowTargets.add(Game.getInstance().getResources().manage(new RenderTarget(1024, 1024, ColorFormat.FLOAT)));
                }

                RenderTarget renderTarget = shadowTargets.get(si++);

                l.calcLightProjection(lightMatrix);
                l.calcLightView(matrix);

                lightMatrix.mul(matrix);
                lightFrustum.set(lightMatrix);

                renderables.clear();
                scene.root.traverse((n) -> {
                    if(n.visible) {
                        if(lightFrustum.testAab(n.bounds.min, n.bounds.max)) {
                            if(n.renderable != null && n.castsShadow) {
                                renderables.add(n);
                            } 
                            return true;
                        }
                    }
                    return false;
                });
        

                renderTarget.begin();
                GFX.clear(Float.MAX_VALUE, 0, 0, 1);
                for(Node node : renderables) {
                    trianglesRendered += node.renderable.renderShadowPass(scene, node, l);
                }
                renderTarget.end();
                l.lightShadowMap = renderTarget;
            }
        }

        renderables.clear();

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

        renderables.sort((a, b) -> {
            if(a.zOrder == b.zOrder) {
                float da = a.absolutePosition.distance(scene.eye);
                float db = b.absolutePosition.distance(scene.eye);

                return Float.compare(db, da);
            } else {
                return Integer.compare(a.zOrder, b.zOrder);
            }
        });

        boolean create = scaleTarget == null;

        if(!create) {
            create = scaleTarget.texture.w != Game.getInstance().w() || scaleTarget.texture.h != Game.getInstance().h();
        }
        if(create) {
            create = Game.getInstance().w() > 50 && Game.getInstance().h() > 50;
        }
        if(create) {
            if(scaleTarget != null) {
                Game.getInstance().getResources().unManage(scaleTarget);
            }
            scaleTarget = Game.getInstance().getResources().manage(new RenderTarget(Game.getInstance().w() / 2, Game.getInstance().h() / 2, ColorFormat.COLOR));
        }

        scaleTarget.begin();

        GFX.clear(scene.backgroundColor.x, scene.backgroundColor.y, scene.backgroundColor.z, scene.backgroundColor.w);

        GL2 gl = Game.getGL();
        
        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1, 1);

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
                lineRenderer.push(-8, 0, 0, 1, 0, 0, 1, 8, 0, 0, 1, 0, 0, 1);
                lineRenderer.push(0, -8, 0, 0, 1, 0, 1, 0, 8, 0, 0, 1, 0, 1);
                lineRenderer.push(0, 0, -8, 0, 0, 1, 1, 0, 0, 8, 0, 0, 1, 1);
                lineRenderer.end();
            } else if(node == axis) {
                float l = scene.calcOffset().length() / 6;

                lineRenderer.begin(scene.projection, scene.view, matrix.identity().translate(scene.target));
                lineRenderer.push(0, 0, 0, 1, 0, 0, 1, l, 0, 0, 1, 0, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 1, 0, 1, 0, l, 0, 0, 1, 0, 1);
                lineRenderer.push(0, 0, 0, 0, 0, 1, 1, 0, 0, l, 0, 0, 1, 1);
                lineRenderer.end();
            }
            trianglesRendered += node.render();
        }
        scaleTarget.end();

        SpriteRenderer spriteRenderer = Game.getInstance().getRenderer(SpriteRenderer.class);

        GFX.clear(0, 0, 0, 1);
        spriteRenderer.begin();
        spriteRenderer.beginSprite(scaleTarget.texture);
        spriteRenderer.push(0, 0, scaleTarget.texture.w, scaleTarget.texture.h, 0, 0, Game.getInstance().w(), Game.getInstance().h(), 1, 1, 1, 1, true);
        spriteRenderer.endSprite();
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
    }
}
