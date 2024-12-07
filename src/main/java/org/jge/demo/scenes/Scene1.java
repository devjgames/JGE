package org.jge.demo.scenes;

import org.jge.Game;
import org.jge.IO;
import org.jge.Mesh;
import org.jge.Node;
import org.jge.Scene;
import org.jge.demo.App.SceneFactory;
import org.jge.demo.components.Player;
import org.jge.demo.components.Torch;

public class Scene1 extends SceneFactory {

    @Override
    public Scene createScene() throws Exception {
        Game game = Game.getInstance();
        Scene scene = new Scene();
        Node node = game.assets.load(IO.file("assets/scene1.obj"));

        node = new Node(node, false);

        node.traverse((n) -> {
            if(n.renderable instanceof Mesh) {
                n.lightMapEnabled = true;
                n.collidable = true;
            }
            return true;
        });

        scene.root.addChild(node);
        scene.lightMapFile = IO.file("lightMaps/scene1.png");

        node = new Node();
        node.position.set(0, 64, 0);
        node.addComponent(scene, new Player());
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightRadius = 200;
        node.position.set(59, 32, -70);
        node.lightColor.set(0.5f, 1, 1.5f, 1);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.position.set(-178, 32, 62);
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.position.set(-204, 160, -317);
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.position.set(54, 160, -218);
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(-178, 32, 32);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(-352, 160, -315);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(32, 160, -204);
        scene.root.addChild(node);

        return scene;
    }
    
}
