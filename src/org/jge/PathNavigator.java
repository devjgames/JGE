package org.jge;

import java.util.HashSet;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PathNavigator {

    private final Vector<PathNode> path = new Vector<>();
    private final HashSet<PathNode> visited = new HashSet<>();
    private PathNode start = null;

    public void findPath(PathNode end) {
        if(start != end && start != null) {
            visited.clear();
            path.clear();
            start.getLocationPath(path, end, visited);
            start = path.firstElement();
        }
    }

    public PathNode getStart() {
        return start;
    }

    public void setStart(PathNode node) {
        start = node;
    }

    public boolean hasPath() {
        return path.size() > 1 && start != null;
    }

    public void move(Vector3f position, float speed, Matrix4f rotation) {
        if(hasPath()) {
            PathNode start = path.firstElement();
            PathNode next = path.get(1);

            float dx = next.position.x - start.position.x;
            float dy = next.position.y - start.position.y;
            float dz = next.position.z - start.position.z;
            float l = Vector2f.length(dx, dz);
            float nx = dx / l;
            float ny = dy / l;
            float nz = dz / l;
            float px = position.x + nx * speed * Game.getInstance().elapsedTime();
            float py = position.y + ny * speed * Game.getInstance().elapsedTime();
            float pz = position.z + nz * speed * Game.getInstance().elapsedTime();
            float tx = px - start.position.x;
            float tz = pz - start.position.z;
            float s = tx * nx + tz * nz;

            if(s >= Vector2f.length(dx, dz)) {
                px = next.position.x;
                py = next.position.y;
                pz = next.position.z;
                path.remove(0);
                if(path.size() == 1) {
                    this.start = path.get(0);
                    path.clear();
                }
            }
            position.set(px, py, pz);

            float radians = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, nx)));

            if(nz > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            rotation.identity().rotate((float)Math.toRadians(-90), 1, 0, 0).rotate(radians, 0, 0, 1);
        }
    }
}
