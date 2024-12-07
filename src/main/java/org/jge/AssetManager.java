package org.jge;

import java.io.File;
import java.util.Hashtable;

public class AssetManager extends Resource {
    
    private static Hashtable<String, AssetLoader> assetLoaders = new Hashtable<>();

    public static void registerAssetLoader(String extension, AssetLoader assetLoader) {
        assetLoaders.put(extension, assetLoader);
    }

    public final ResourceManager resources = new ResourceManager();
    
    private Hashtable<String, Object> assets = new Hashtable<>();

    public AssetManager() {
        registerAssetLoader(".png", new Texture.Loader());
        registerAssetLoader(".wav", new SoundLoader());
        registerAssetLoader(".obj", new NodeLoader());
        registerAssetLoader(".kfm", new KeyFrameMesh.KeyFrameMeshLoader());
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(File file) throws Exception {
        String key = file.getPath();

        if(!assets.containsKey(key)) {
            System.out.println("loading asset '" + key + "' ...");
            assets.put(key, assetLoaders.get(IO.getExtension(file)).load(file, this));
        }
        return (T)assets.get(key);
    }

    public void unload(File file) throws Exception {
        String key = file.getPath();

        if(assets.containsKey(key)) {
            Object asset = assets.get(key);

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
            assets.remove(key);
        }
    }

    public void clear() throws Exception {
        resources.clear();

        for(String key : assets.keySet()) {
            Object asset = assets.get(key);

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
        }
        assets.clear();
    }

    @Override
    public void destroy() throws Exception {
        clear();
        resources.destroy();
        super.destroy();
    }
}
