package org.game.kits.pf;

import java.io.File;

import org.game.AABB;
import org.game.BasicRenderer;
import org.game.BasicRenderer2;
import org.game.Collidable;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.MeshLoader;
import org.game.Mesh;
import org.game.MeshPart;
import org.game.RenderPass;
import org.game.Vec3;
import org.game.Vec4;

public class Model extends Collidable {

    private String meshName;

    public Model(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public boolean isCollidable() {
        return !meshName.equals("axis.obj") && !meshName.equals("player.obj") && !meshName.equals("sky.obj");
    }

    @Override
    public int getCollidableTag() {
        return 0;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        Mesh mesh = gs.game.getResources().load(new File(meshName));

        if(meshName.equals("sky.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(0, 0, 0, 1));
        } else if(meshName.equals("axis.obj") || meshName.equals("player.obj") || meshName.equals("gem.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
            if(meshName.equals("player.obj")) {
                node.addChild(new GameNode());
                node.getChild(0).position.y = -10;
            }
        } else {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(0.5f, 0.5f, 0.5f, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        }
        if(!meshName.equals("axis.obj") && !meshName.equals("player.obj")) {
            AABB b = new AABB();

            for(int i = 0; i != mesh.getPartCount(); i++) {
                MeshPart part = mesh.getPart(i);

                b.union(part.getIBounds());
            }
            node.addChild(new GameNode());
            if(meshName.equals("sky.obj")) {
                node.scale.mul(5);
            } else {
                node.scale.mul((meshName.equals("rock.obj")) ? 0.5f : 0.25f);
            }
            b.center(node.getChild(0).position).negate();
        }
        node.data.put("mesh", mesh);
        node.data.put("visible", true);
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        Mesh mesh = (Mesh)node.data.get("mesh");

        if(!meshName.equals("axis.obj")) {
            mesh.calcBoundsAndTransform(node.getChild(0));
        } else {
            mesh.calcBoundsAndTransform(node);
        }

        return mesh.pick(origin, direction, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign()) {
            return ;
        }

        if(meshName.equals("gem.obj")) {
            node.rotate(1, 90 * gs.game.getElapsedTime());
        }
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer basicRenderer, RenderPass renderPass) throws Exception {
        if((meshName.equals("axis.obj") && !gs.inDesign()) && !(this instanceof Player)) {
            return;
        }
        if(renderPass != RenderPass.SOLID) {
            return;
        }
        if((Boolean)node.data.get("visible") == false) {
            return ;
        }

        Mesh mesh = (Mesh)node.data.get("mesh");

        if(!meshName.equals("axis.obj")) {
            mesh.calcBoundsAndTransform(node.getChild(0));
        } else {
            mesh.calcBoundsAndTransform(node);
        }

        if(gs.frustum.testAab(mesh.bounds)) {
            for(int j = 0; j != mesh.getPartCount(); j++) {
                MeshPart part = mesh.getPart(j);

                if(gs.frustum.testAab(part.bounds)) {

                    basicRenderer.setModel(mesh.model);
                    basicRenderer.setModelIT(mesh.modelIT);
                    basicRenderer.setAmbientColor(mesh.get(MeshLoader.AMBIENT_COLOR));
                    basicRenderer.setDiffuseColor(mesh.get(MeshLoader.DIFFUSE_COLOR));
                    basicRenderer.setTexture(part.get(MeshLoader.TEXTURE));

                    BasicRenderer2 br2 = (BasicRenderer2)basicRenderer;

                    br2.setDecal(null);
                    basicRenderer.setLightingEnabled(true);
                    br2.setWarp(false, 0, 0, 0, false);

                    basicRenderer.draw(part.vertexBuffer, part.vertexElementBuffer);
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return meshName;
    }
}

