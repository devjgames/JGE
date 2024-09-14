package org.jge;

import java.io.File;

public class SoundLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        return new Sound(file);
    }
    
}
