package org.jge.demo;

import org.jge.GameEditor;
import org.jge.Utils;

public class App {
    
    public static void main(String[] args) throws Exception {
        Utils.setLookAndFeel();

        new GameEditor(1000, 700, true, true,
            Player.class,
            Torch.class,
            Drip.class
        );
    }
}
