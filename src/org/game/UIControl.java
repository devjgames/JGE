package org.game;

/**
 * UIControl
 */
abstract class UIControl {

    private int x = 0;
    private int y = 0;

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public abstract int w(UI ui);

    public abstract int h(UI ui);

    public abstract void draw(UI ui);

    public boolean hitTest(UI ui, int x, int y) {
        return x >= x() && x <= x() + w(ui) && y >= y() && y <= y() + h(ui);
    }

    public void update(UI ui) {
    }

    public void mouseDown(UI ui, int x, int y) {
    }

    public void mouseUp(UI ui, int x, int y) {
    }

    public void mouseMoved(UI ui, int x, int y) {
    }

    public void keyDown(UI ui, int key) {
    }

    public void keyUp(UI ui, int key) {
    }

    public void charPressed(UI ui, char c) {
    }
}