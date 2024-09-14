package org.jge.demo;

import org.jge.GameEditor;
import org.jge.Utils;

public class App {
    
    public static void main(String[] args) throws Exception {
        Utils.setLookAndFeel();

        new GameEditor(800, 500, "org.jge.demo", true, true,
            Player.class.getSimpleName(),
            Rotator.class.getSimpleName(),
            Sky.class.getSimpleName(),
            Torch.class.getSimpleName()
        );
    }
}
