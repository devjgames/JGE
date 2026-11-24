package org.game;

public abstract class Collidable extends GameComponent {
    
    public abstract boolean isCollidable();

    public abstract int getCollidableTag();
}
