package org.game;

class UIText extends UIControl {

    private String text = "";
    private int maxChars;
    private int start = 0;
    private int location = 0;
    private String changed = null;

    public UIText(int maxChars) {
        this.maxChars = Math.max(2, maxChars);
    }

    public void setText(String text) {
        this.text = this.changed = text;

        start = 0;
        location = 0;
    }

    public String changed() {
        String c = changed;

        changed = null;

        return c;
    }

    @Override
    public int w(UI ui) {
        return maxChars * ui.charW + 16;
    }

    @Override
    public int h(UI ui) {
        return ui.charH + 16;
    }

    @Override
    public void draw(UI ui) {
        ui.drawRect(
            x(), y(), maxChars * ui.charW + 16, ui.charH + 16, 
            UIColors.TEXT_BACKGROUND, UIColors.TEXT_BACKGROUND, UIColors.BORDER
        );

        if(!text.isEmpty()) {
            ui.drawString(text.substring(start, start + Math.min(maxChars, text.length() - start)), x() + 8, y() + 8, maxChars, UIColors.TEXT);
        }
        if(ui.getFocus() == this) {
            ui.drawRect(x() + 8 + (location - start) * ui.charW, y() + 8, ui.charW, ui.charH, UIColors.TEXT, UIColors.TEXT, UIColors.TEXT);
            if(location < text.length()) {
                ui.drawString(text.substring(location, location + 1), x() + 8 + (location - start) * ui.charW, y() + 8, 1, UIColors.TEXT_BACKGROUND);
            }
        }
    }

    @Override
    public void mouseDown(UI ui, int x, int y) {
        if(hitTest(ui, x, y)) {
            int dx = x - (x() + 8);
            int index = start + (dx < 0 ? 0 : dx / ui.charW);

            location = Math.max(0, Math.min(index, text.length()));
            scroll();
        }
    }

    @Override
    public void keyDown(UI ui, int key) {
        if(key == Keys.KEY_BACKSPACE) {
            if(location > 0) {
                text = text.substring(0, location - 1) + text.substring(location);
                location--;
                changed = text;
                scroll();
            }
        } else if(key == Keys.KEY_LEFT) {
            if(location > 0) {
                location--;
                scroll();
            }
        } else if(key == Keys.KEY_RIGHT) {
            if(location < text.length()) {
                location++;
                scroll();
            }
        }
    }

    @Override
    public void charPressed(UI ui, char c) {
        if(c < 32 || c == 127) {
            return;
        }

        text = text.substring(0, location) + c + text.substring(location);
        location++;
        changed = text;
        scroll();
    }

    private void scroll() {
        if(location < start) {
            start = location;
        }
        if(location - start > maxChars - 1) {
            start = location - maxChars + 1;
        }
        int maxStart = Math.max(0, text.length() - maxChars + 1);
        if(start > maxStart) {
            start = maxStart;
        }
    }
}
