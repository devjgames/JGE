package org.jge.demo.scenes;

import org.jge.Game;
import org.jge.IO;
import org.jge.KeyFrameMesh;
import org.jge.Mesh;
import org.jge.Node;
import org.jge.Scene;
import org.jge.demo.App.SceneFactory;
import org.jge.demo.components.Player;
import org.jge.demo.components.Torch;

public class Scene2 extends SceneFactory {
    
    @Override
    public Scene createScene() throws Exception {
        Game game = Game.getInstance();
        Scene scene = new Scene();
        Node node = game.assets.load(IO.file("assets/scene2.obj"));

        node = new Node(node, false);

        node.traverse((n) -> {
            if(n.renderable instanceof Mesh) {
                n.lightMapEnabled = true;
                n.collidable = true;
            }
            return true;
        });

        scene.root.addChild(node);

        scene.lightMapFile = IO.file("lightMaps/scene2.png");

        node = new Node();
        node.position.set(0, 64, 0);
        node.addComponent(scene, new Player());
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightColor.set(1.5f, 1, 0.5f, 1);
        node.position.set(0, 32, 0);
        scene.root.addChild(node);

        node = new Node();
        node.isLight = true;
        node.lightColor.set(0.5f, 1, 1.5f, 1);
        node.position.set(345, 64, 0);
        scene.root.addChild(node);

        node = new Node();
        node.addComponent(scene, new Torch());
        node.position.set(5, 31, -96);
        scene.root.addChild(node);

        KeyFrameMesh mesh = game.assets.load(IO.file("assets/mage.kfm"));
        Node child = new Node();

        node = new Node();
        node.position.set(420, 32, 0);
        child.renderable = mesh.newInstance(false);
        child.scale.set(0.05f, 0.05f, 0.05f);
        child.position.y = -mesh.getBounds().min.y * 0.05f;
        child.texture = game.assets.load(IO.file("assets/female_mage_texture.png"));
        child.rotation.rotate((float)-Math.PI / 2, 0, 1, 0);
        child.collidable = true;
        child.dynamic = true;
        node.addChild(child);
        scene.root.addChild(node);

        return scene;
    }
}
