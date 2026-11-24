package org.game;

public class VertexAttribute {
    
    private final String name;
    private final int size;

    public VertexAttribute(String name, int size) throws Exception {
        this.name = name;
        this.size = size;

        if(size < 1 || size > 4) {
            throw new Exception("invalid vertex attribute size");
        }
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
