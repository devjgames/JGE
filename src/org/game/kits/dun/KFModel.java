package org.game.kits.dun;

import java.io.File;

import org.game.BasicRenderer;
import org.game.BasicRenderer2;
import org.game.Collidable;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.KeyFrameMesh;
import org.game.MeshLoader;
import org.game.MeshPart;
import org.game.RenderPass;
import org.game.Vec3;
import org.game.Vec4;

public class KFModel extends Collidable {

    private static final String AMBIENT_COLOR = "ambientColor";
    private static final String DIFFUSE_COLOR = "diffuseColor";
    
    private String meshName;

    public KFModel(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshName() {
        return meshName;
    }
    
    @Override
    public boolean isCollidable() {
        return meshName.equals("Monster");
    }

    @Override
    public int getCollidableTag() {
        if(meshName.equals("Monster")) {
            return Player.COLLIDABLE_DEATH;
        }
        return Player.COLLIDABLE_DEFAULT;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = gs.game.getResources().load(new File(meshName));

        mesh = gs.game.getResources().manage(new KeyFrameMesh(mesh));
        mesh.setSequence(0, mesh.getCount() - 1, 16, true);

        if(meshName.equals("Monster")) {
            node.data.put(AMBIENT_COLOR, new Vec4(0.65f, 0.65f,0.65f, 1));
            node.data.put(DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        } else {
            node.data.put(AMBIENT_COLOR, new Vec4(0.5f, 0.5f,0.5f, 1));
            node.data.put(DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        }

        node.data.put("mesh", mesh);

    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        return mesh.getMesh().pick(origin, direction, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        mesh.update();
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {
        if(gs.inDesign() || Player.started()) {
            KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

            mesh.getMesh().calcBoundsAndTransform(node);

            if(gs.frustum.testAab(mesh.getMesh().bounds)) {
                for(int i = 0; i != mesh.getMesh().getPartCount(); i++) {
                    MeshPart part = mesh.getMesh().getPart(i);

                    if(gs.frustum.testAab(part.bounds)) {
                        renderer.setTexture(part.get(MeshLoader.TEXTURE));
                        renderer.setModel(mesh.getMesh().model);
                        renderer.setModelIT(mesh.getMesh().modelIT);
                        renderer.setLightingEnabled(true);

                        renderer.setAmbientColor((Vec4)node.data.get(AMBIENT_COLOR));
                        renderer.setDiffuseColor((Vec4)node.data.get(DIFFUSE_COLOR));

                        BasicRenderer2 br2 = (BasicRenderer2)renderer;

                        br2.setWarp(false, 0, 0, 0, false);
                        br2.setDecal(null);

                        renderer.draw(part.vertexBuffer, part.vertexElementBuffer);
                    }
                }
            }
        }
    }


    @Override
    public String toString() {
        return meshName;
    }
}
