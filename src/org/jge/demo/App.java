package org.jge.demo;

import org.jge.GameEditor;
import org.jge.Utils;

public class App {
    
    public static void main(String[] args) throws Exception {
        Utils.setLookAndFeel();

        new GameEditor(800, 500, true, false,
            Player.class,
            Rotator.class,
            Sky.class,
            Torch.class
        );
    }
}
