package org.jge;

import java.io.File;

public interface AssetLoader {

    Object load(File file, AssetManager assets) throws Exception;
    
}
