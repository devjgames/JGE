package org.jge;

import java.io.File;

public class InstanceLoader implements AssetLoader {
    
    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        Node node = assets.load(IO.file(file.getParentFile(), IO.getFilenameWithoutExtension(file) + ".obj"));
        Instance instance = new Instance(file);

        for(Node child : node) {
            Mesh mesh = (Mesh)child.renderable;
            int bV = instance.vertices.size();

            for(VertexPTN vertex : mesh.vertices) {
                instance.vertices.add(vertex);
            }
            for(Integer i : mesh.indices) {
                instance.indices.add(bV + i);
            }
        }
        instance.calcBounds();

        return instance;
    }
    
}
