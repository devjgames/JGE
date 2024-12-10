package org.jge.demo.scenes;

import org.jge.Game;
import org.jge.IO;
import org.jge.Mesh;
import org.jge.Node;
import org.jge.Scene;
import org.jge.demo.App.SceneFactory;
import org.jge.demo.components.Player;
import org.jge.demo.components.Torch;

public class Scene5 extends SceneFactory {

    @Override
    public Scene createScene() throws Exception {
        Game game = Game.getInstance();
        Scene scene = new Scene();
        Node node = game.assets.load(IO.file("assets/scene5.obj"));

        node = new Node(node, false);
        node.traverse((n) -> {
            if(n.renderable instanceof Mesh) {
                n.collidable = true;
                n.lightMapEnabled = true;
            }
            return true;
        });
        scene.root.addChild(node);
        scene.lightMapFile = IO.file("lightMaps/scene5.png");
        
        Player player = new Player();

        player.direction.set(0, 0, -1);
        node = new Node();
        node.addComponent(scene, player);
        node.position.set(0, 64, 0);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightRadius = 400;
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        node.position.set(0, 33, -65);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightColor.set(0.5f, 1, 1.5f, 1);
        node.position.set(-259, 96, -370);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        node.position.set(-458, 128, -120);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(-428, 124, -160);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(-32, 43, -104);
        scene.root.addChild(node);

        return scene;
    }
    
}
