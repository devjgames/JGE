package org.jge;

import java.util.HashSet;

public class ResourceManager extends Resource {
    
    private HashSet<Resource> resources = new HashSet<>();

    public <T extends Resource> T manage(T resource) {
        resources.add(resource);

        return resource;
    }

    public void unManage(Resource resource) throws Exception {
        if(resources.remove(resource)) {
            resource.destroy();
        }
    }

    public void clear() throws Exception {
        for(Resource resource : resources) {
            resource.destroy();
        }
        resources.clear();
    }

    @Override
    public void destroy() throws Exception {
        clear();
        super.destroy();
    }
}
