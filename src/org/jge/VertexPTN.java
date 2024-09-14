package org.jge;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class VertexPTN {
    
    public final Vector3f position = new Vector3f();
    public final Vector2f textureCoordinate = new Vector2f();
    public final Vector3f normal = new Vector3f();

    public VertexPTN newInstance() throws Exception {
        VertexPTN v = new VertexPTN();

        Utils.copy(this, v);

        return v;
    }
}
