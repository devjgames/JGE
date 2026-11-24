package org.game.kits.pf;

import org.game.GameLight;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Mat4;
import org.game.Vec4;

public class Torch extends Model {

    private Vec4 color;
    private Mat4 model = new Mat4();

    public Torch(float r, float g, float b, float a) {
        super("axis.obj");

        color = new Vec4(r, g, b, a);
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        if(i == 0) {
            GameLight light = (GameLight)node.data.get("gameLight");

            if(light == null) {
                light = new GameLight();

                light.color.set(color);
                node.data.put("gameLight", light);
            }

            light.radius = (Float)node.data.get("radius");

            node.calcModel(model);

            light.position.set(0, 0, 0).mulPosition(model);

            return light;
        }
        return null;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        if(!node.data.containsKey("radius")) {
            float r = 50;

            node.data.put("radius", r);
            node.value = "" + r;
        }
    }


    @Override
    public void parseValue(GameSystem gs, GameNode node) throws Exception {
        try {
            node.data.put("radius", Float.parseFloat(node.value.trim()));
        } catch(NumberFormatException ex) {

        }
    }

    @Override
    public String toString() {
        return "torch";
    }
}
