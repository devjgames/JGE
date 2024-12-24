package org.quest.components;

import org.jge.Game;
import org.jge.Mesh;
import org.jge.NodeComponent;
import org.jge.VertexPT2N;
import org.joml.Vector3f;

public class Warp extends NodeComponent {

    public float amplitude = 8;
    public float freq = 0.05f;
    
    private VertexPT2N[][] baseVertices;

    @Override
    public void init() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        Mesh mesh = (Mesh)node().renderable;

        baseVertices = new VertexPT2N[mesh.parts.size()][];

        for(int i = 0; i != mesh.parts.size(); i++) {
            baseVertices[i] = new VertexPT2N[mesh.parts.get(i).vertices.size()];

            for(int j = 0; j != mesh.parts.get(i).vertices.size(); j++) {
                VertexPT2N vertex = mesh.parts.get(i).vertices.get(j);

                baseVertices[i][j] = vertex.newInstance();
            }
        }
    }

    @Override
    public void update() throws Exception {
        if(scene().isInDesign()) {
            return;
        }

        Mesh mesh = (Mesh)node().renderable;
        Game game = Game.getInstance();
        float t = game.totalTime();

        for(int i = 0; i != mesh.parts.size(); i++) {
            for(int j = 0; j != mesh.parts.get(i).vertices.size(); j++) {
                VertexPT2N baseVertex = baseVertices[i][j];
                VertexPT2N vertex = mesh.parts.get(i).vertices.get(j);
                Vector3f p = baseVertex.position;

                vertex.position.x = p.x + amplitude * (float)(Math.sin(p.z * freq + t) * Math.sin(p.y * freq + t));
                vertex.position.y = p.y + amplitude * (float)(Math.sin(p.z * freq + t) * Math.sin(p.x * freq + t));
                vertex.position.z = p.z + amplitude * (float)(Math.sin(p.x * freq + t) * Math.sin(p.y * freq + t));
            }
            mesh.parts.get(i).calcBounds();
        }
        mesh.calcBounds();
    }
}
