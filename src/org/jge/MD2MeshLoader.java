package org.jge;

import java.io.File;

public class MD2MeshLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        return new MD2Mesh(file);
    }
    
}
