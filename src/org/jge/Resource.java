package org.jge;

public class Resource {
    
    private static int instances = 0;

    public static int getInstances() {
        return instances;
    }

    public Resource() {
        instances++;
    }

    public void destroy() throws Exception {
        instances--;
    }
}
