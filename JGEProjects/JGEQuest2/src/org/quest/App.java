package org.quest;

import org.jge.GameEditor;
import org.jge.Utils;
import org.jge.demo.Drip;
import org.quest.components.FireLight;
import org.quest.components.Player;
import org.quest.components.Sky;
import org.quest.components.Warp;

public class App {
    
    public static void main(String[] args) throws Exception {
        Utils.setLookAndFeel();

        new GameEditor(1000, 700, true, true,
            Drip.class,
            Player.class,
            Warp.class,
            FireLight.class,
            Sky.class
        );
    }
}
