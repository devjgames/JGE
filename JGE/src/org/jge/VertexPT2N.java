package org.jge;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class VertexPT2N {
    
    public final Vector3f position = new Vector3f();
    public final Vector2f textureCoordinate = new Vector2f();
    public final Vector2f textureCoordinate2 = new Vector2f();
    public final Vector3f normal = new Vector3f();

    public VertexPT2N newInstance() throws Exception {
        VertexPT2N v = new VertexPT2N();

        Utils.copy(this, v);

        return v;
    }
}
