package org.quest;

import org.jge.GameEditor;
import org.jge.Utils;
import org.jge.demo.Torch;
import org.quest.components.Door;
import org.quest.components.Item;
import org.quest.components.Player;

public class App {
    
    public static void main(String[] args) throws Exception {
        Utils.setLookAndFeel();

        new GameEditor(1000, 700, true, true,
            Torch.class,
            Item.class,
            Player.class,
            Door.class
        );
    }
}
