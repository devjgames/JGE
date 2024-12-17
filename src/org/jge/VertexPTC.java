package org.jge;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class VertexPTC {
    
    public final Vector3f position = new Vector3f();
    public final Vector2f textureCoordinate = new Vector2f();
    public final Vector4f color = new Vector4f();

    public VertexPTC newInstance() throws Exception {
        VertexPTC v = new VertexPTC();

        Utils.copy(this, v);

        return v;
    }
}
