package org.jge.demo.scenes;

import org.jge.Game;
import org.jge.IO;
import org.jge.Mesh;
import org.jge.Node;
import org.jge.Scene;
import org.jge.demo.App.SceneFactory;
import org.jge.demo.components.Door;
import org.jge.demo.components.Player;

public class Scene4 extends SceneFactory {

    @Override
    public Scene createScene() throws Exception {
        Game game = Game.getInstance();
        Scene scene = new Scene();
        Node node = game.assets.load(IO.file("assets/scene4.obj"));

        node = new Node(node, true);
        node.traverse((n) -> {
            if(n.renderable instanceof Mesh) {
                n.collidable = true;
                n.diffuseColor.set(0, 0, 0, 1);
                n.ambientColor.set(0.25f, 0.25f, 0.25f, 1);
            }
            if(n.name.equals("Door")) {
                n.addComponent(scene, new Door());
            }
            return true;
        });
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Player());
        node.position.set(0, 64, 0);
        scene.root.addChild(node);

        return scene;
    }
    
}
