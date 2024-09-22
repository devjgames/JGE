package org.jge;

import java.io.File;

public class Mesh extends Instance {


    public Mesh(File file) {
        super(file);
    }

    @Override
    public Renderable newInstance() throws Exception {
        Mesh mesh = new Mesh(getFile());

        for(VertexPTN vertex : vertices) {
            mesh.vertices.add(vertex.newInstance());
        }
        for(Integer i : indices) {
            mesh.indices.add(i);
        }
        mesh.calcBounds();

        return mesh;
    }

}
