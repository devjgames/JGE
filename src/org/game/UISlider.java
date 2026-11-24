package org.game;

class UISlider extends UIControl {

    private int w;
    private float x = 0;
    private boolean drag = false;

    public UISlider(int w) {
        this.w = w;
    }

    public void setValue(float value) {
        x = Math.max(0, Math.min(1, value));
    }

    public float value() {
        return x;
    }

    @Override
    public int w(UI ui) {
        return w * ui.charW + 16;
    }

    @Override
    public int h(UI ui) {
        return ui.charH + 16;
    }

    private int l(UI ui) {
        return w(ui) - ui.charW;
    }

    private int tx(UI ui) {
        return Math.max(0, Math.min(l(ui), (int)(x * l(ui))));
    }

    @Override
    public void draw(UI ui) {
        ui.drawHLine(x(), x() + w(ui), y() + h(ui) / 2, UIColors.BORDER);
        ui.drawRect(x() + tx(ui), y(), ui.charW, ui.charH + 16, UIColors.BUTTON2, UIColors.BUTTON1, UIColors.BORDER);
    }
    

    @Override
    public void mouseDown(UI ui, int x, int y) {
        if(hitTest(ui, x, y)) {
            int x1 = x() + tx(ui);
            int y1 = y();
            int x2 = x1 + ui.charW;
            int y2 = y1 + ui.charH + 16;

            if(x >= x1 && x <= x2 && y >= y1 && y < y2) {
                drag = true;
            }
        }
    }

    @Override
    public void mouseMoved(UI ui, int x, int y) {
        if(drag) {
            this.x = Math.max(0, Math.min(1, (x - x()) / (float)l(ui)));
        }
    }

    @Override
    public void mouseUp(UI ui, int x, int y) {
        drag = false;
    }
}