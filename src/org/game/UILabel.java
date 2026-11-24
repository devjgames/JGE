package org.game;

class UILabel extends UIControl {

    private String text = "";
    private int maxChars;

    public UILabel(int maxChars) {
        this.maxChars = maxChars;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if(text != null) {
            this.text = text;
        }
    }

    @Override
    public int w(UI ui) {
        return text.length() * ui.charW;
    }

    @Override
    public int h(UI ui) {
        return ui.charH + 16;
    }

    @Override
    public void draw(UI ui) {
        ui.drawString(text, x(), y() + 8, maxChars, UIColors.LABEL);
    }
    
}
