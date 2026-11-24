package org.game;

import java.util.Vector;

class UIList extends UIControl {

    private int rows;
    private int cols;
    private Vector<Object> items = null;
    private int start = 0;
    private int selected = -1;
    private float thumb = 0;
    private int di = -1;
    private boolean drag = false;
    private int lastCount = 0;

    public UIList(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public void draw(UI ui) {
        setItems(items);
        
        ui.drawRect(x(), y(), w(ui), h(ui), UIColors.TEXT_BACKGROUND, UIColors.TEXT_BACKGROUND, UIColors.BORDER);
        if(items != null) {
            int count = Math.min(items.size(), rows);

            for(int i = 0; i != count; i++) {
                int j = start + i;
                Vec4 color = UIColors.TEXT;

                if(j == selected) {
                    ui.drawRect(
                        x() + 4, y() + i * (ui.charH + 16) + 4, cols * ui.charW + 8, ui.charH + 8,
                         UIColors.TEXT, UIColors.TEXT, UIColors.TEXT
                        );
                    color = UIColors.TEXT_BACKGROUND;
                }
                ui.drawString(items.get(j).toString(), x() + 8, y() + i * (ui.charH + 16) + 8, cols, color);
            }
            if(items.size() > rows) {
                ui.drawRect(
                    x() + 16 + cols * ui.charW, y() + (int)(thumb * (h(ui) - (ui.charH + 16))), ui.charW, ui.charH + 16,
                    UIColors.BORDER, UIColors.BORDER, UIColors.BORDER
                );
            }
        }
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int index) {
        if(items != null) {
            if(index >= -1 && index < items.size() - 1) {
                selected = index;
            }
        }
    }

    @Override
    public int w(UI ui) {
        return cols * ui.charW + 16 + ui.charW;
    }

    @Override
    public int h(UI ui) {
        return rows * (ui.charH + 16);
    }

    public void setItems(Vector<Object> items) {
        boolean reset = this.items == null;

        if(!reset) {
            reset = lastCount != items.size();
        }
        this.items = items;
        lastCount = items.size();

        if(reset) {
            start = 0;
            selected = -1;
            thumb = 0;
        }
    }

    @Override 
    public void mouseDown(UI ui, int x, int y) {
        if(hitTest(ui, x, y) && items != null) {
            if(x < x() + 16 + ui.charW * cols) {
                int i = (y - y()) / (ui.charH + 16) + start;

                if(i < items.size()) {
                    di = i;
                }
            } else if(items.size() > rows) {
                int x1 = x() + 16 + ui.charW * cols;
                int x2 = x1 + ui.charW;
                int y1 = y() + (int)(thumb * (h(ui) - (ui.charH + 16)));
                int y2 = y1 + ui.charH + 16;

                if(x >= x1 && x <= x2 && y >= y1 && y <= y2) {
                    drag = true;
                }
            }
        }
    }

    @Override
    public void mouseUp(UI ui, int x, int y) {
        if(hitTest(ui, x, y)) {
            if(x < x() + 16 + ui.charW * cols) {
                int i = (y - y()) / (ui.charH + 16) + start;

                if(di == i) {
                    selected = di;
                }
            }
        }
        di = -1;
        drag = false;
    }

    @Override
    public void mouseMoved(UI ui, int x, int y) {
        if(drag) {
            y = Math.max(0, y - y());
            thumb = Math.min(1, y / (float)(h(ui) - (ui.charH + 16)));
            start = Math.max(0, Math.min((int)(thumb * (items.size() - rows)), items.size() - rows));
        }
    }
}