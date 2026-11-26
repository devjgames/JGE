package org.game.kits.dun;

import java.io.File;

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

    private String directory;
    private String meshName;

    public Model(String directory, String meshName) {
        this.directory = directory;
        this.meshName = meshName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public boolean isCollidable() {
        return 
            !meshName.equals("axis.obj") &&
            !meshName.equals("coins.obj");
    }

    @Override
    public int getCollidableTag() {
        if(meshName.equals("lava.obj")) {
            return Player.COLLIDABLE_DEATH;
        } else if(meshName.equals("key.obj") || meshName.equals("heal.obj")) {
            return Player.COLLIDABLE_COLLECT;
        } else {
            return Player.COLLIDABLE_DEFAULT;
        }
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        Mesh mesh;
        
        if(directory.isBlank()) {
            mesh = gs.game.getResources().load(new File(meshName));
        } else {
            mesh = gs.game.getResources().load(new File(directory, meshName));
        }

        if(meshName.equals("door.obj")) {
            node.data.put("y", node.position.y);
        } else if(meshName.equals("axis.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
            node.scale.mul(0.5f);
        } else if(meshName.equals("key.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(0.5f, 0.5f, 0.5f, 1));
        } else if(meshName.equals("heal.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        } else if(meshName.equals("orb.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));         
        } else if(meshName.equals("lava.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(0.25f, 0.25f, 0.25f, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        } else {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(0.4f, 0.4f, 0.4f, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));
        }
        node.data.put("mesh", mesh);
        node.data.put("value", node.value);
        node.data.put("visible", true);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign() || !(Boolean)node.data.get("visible")) {
            return;
        }

        if(meshName.equals("door.obj")) {
            float ex = gs.eye.x;
            float ez = gs.eye.z;
            float px = node.position.x;
            float pz = node.position.z;
            float dx = ex - px;
            float dz = ez - pz;
            float d = (float)Math.sqrt(dx * dx + dz * dz);
            float a = 1 - Math.min(d / 60, 1);
            float y = (Float)node.data.get("y");

            node.position.y = y - a * 150;
        } else if(getCollidableTag() == Player.COLLIDABLE_COLLECT) {
            node.rotate(1, 45 * gs.game.getElapsedTime());
        }
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        Mesh mesh = (Mesh)node.data.get("mesh");

        mesh.calcBoundsAndTransform(node);

        return mesh.pick(origin, direction, time);
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer basicRenderer, RenderPass renderPass) throws Exception {
        if(meshName.equals("axis.obj") && !gs.inDesign()) {
            return;
        }
        if(renderPass != RenderPass.SOLID) {
            return;
        }
        if((Boolean)node.data.get("visible") == false) {
            return ;
        }

        Mesh mesh = (Mesh)node.data.get("mesh");

        mesh.calcBoundsAndTransform(node);

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

                    if(meshName.equals("lava.obj")) {
                        br2.setWarp(true, gs.game.getTotalTime() * 2, 4, 0.1f, true);
                    } else if(meshName.equals("orb.obj")) {
                        br2.setWarp(true, gs.game.getTotalTime() * 2, 1, 0.35f, true);
                    } else {
                        br2.setWarp(false, 0, 0, 0, false);
                    }

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
