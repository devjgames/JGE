package org.game;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;

public class ResourceManager extends Resource {

    public static interface Loader {

        public Object load(File file, String extension) throws Exception;
    }
    
    private File root = new File("assets");
    private final HashSet<Resource> resources = new HashSet<>();
    private final Hashtable<File, Resource> keyedResources = new Hashtable<>();
    private Loader loader = null;

    public File getRoot() {
        return root;
    }

    public void setRoot(File directory) {
        if(directory != null) {
            root = directory;
        }
    }

    public <T extends Resource> T manage(T resource) {
        resources.add(resource);

        return resource;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> T load(File file) throws Exception {
        file = new File(root, file.getPath());

        if(!keyedResources.containsKey(file)) {
            Log.getInstance().put(1, "loading resource '" + file + "' ...");

            String extension = file.getName();
            int i = extension.lastIndexOf('.');

            if(i >= 0) {
                extension = extension.substring(i);
            } else {
                extension = "";
            }

            if(extension.equals(".png")) {
                keyedResources.put(file, Texture.load(file));
            } else if(extension.equals(".obj")) {
                keyedResources.put(file, MeshLoader.load(file));
            } else if(extension.equals(".wav")) {
                keyedResources.put(file, new SoundEngine.Sound(file));
            } else {
                T resource = null;

                if(loader != null) {
                    resource = (T)loader.load(file, extension);
                }
                if(resource != null) {
                    keyedResources.put(file, resource);
                } else {
                    throw new Exception("unsupported asset extension '" + extension + "'");
                }
            }
            manage(keyedResources.get(file));
        }
        return (T)keyedResources.get(file);
    }
    
    public void unManage(Resource resource) {
        resources.remove(resource);
        resource.destroy();
    }

    public void clear() {
        for(Resource resource : resources) {
            resource.destroy();
        }
        resources.clear();
        keyedResources.clear();
    }

    @Override
    public void destroy() {
        clear();
        super.destroy();
    }
}
