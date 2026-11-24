package org.game;

import java.util.Hashtable;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class UI {
    
    final int scale;
    final int columns;
    final int charW;
    final int charH;
    final int whiteX;
    final int whiteY;

    private SpriteRenderer spriteRenderer;
    private Texture font;
    private final Hashtable<String,  UIControl> controls = new Hashtable<>();
    private final Vector<UIControl> visible = new Vector<>();
    private boolean down = false;
    private UIControl focus = null;
    private int x = 0;
    private int y = 0;
    private int sx = 0;
    private int maxH = 0;
    private UI me;

    public UI(SpriteRenderer spriteRenderer, Texture font, int columns, int charW, int charH, int whiteX, int whiteY) {
        me = this;

        this.spriteRenderer = spriteRenderer;
        this.font = font;
        this.scale = Game.getInstance().getScale();
        this.columns = columns;
        this.charW = charW;
        this.charH = charH;
        this.whiteX = whiteX;
        this.whiteY = whiteY;

        GLFW.glfwSetCharCallback(Game.getInstance().getWindow(), new GLFWCharCallbackI() {

            @Override
            public void invoke(long window, int c) {
                if(focus != null) {
                    if(c >= 32 && c <= 127) {
                        focus.charPressed(me, (char)c);
                    }
                }
            }
            
        });
        GLFW.glfwSetKeyCallback(Game.getInstance().getWindow(), new GLFWKeyCallbackI() {

            @Override
            public void invoke(long window, int key, int scanCode, int action, int mods) {
                if(focus != null) {
                    if(action == GLFW.GLFW_PRESS) {
                        focus.keyDown(me, key);
                    } else if(action == GLFW.GLFW_RELEASE) {
                        focus.keyUp(me, key);
                    }
                }
            }
            
        });
    }

    UIControl getFocus() {
        return focus;
    }

    public void setFont(Texture font) {
        if(font != null) {
            this.font = font;
        }
    }

    public void setSpriteRenderer(SpriteRenderer spriteRenderer) {
        if(spriteRenderer != null) {
            this.spriteRenderer = spriteRenderer;
        }
    }

    void drawHLine(int x1, int x2, int y, Vec4 color) {
        spriteRenderer.push(
            whiteX, whiteY, 1, 1, 
            x1 * scale, y * scale, (x2 - x1) * scale, scale, 
            color, color, false
        );
    }

    void drawVLine(int x, int y1, int y2, Vec4 color) {
        spriteRenderer.push(
            whiteX, whiteY, 1, 1, 
            x * scale, y1 * scale, scale, (y2 - y1) * scale, 
            color, color, false
        );
    }

    void drawRect(int x, int y, int w, int h, Vec4 color1, Vec4 color2, Vec4 borderColor) {
        spriteRenderer.push(whiteX, whiteY, 1, 1, x * scale, y * scale, w * scale, h * scale, color1, color2, false);
        drawHLine(x, x + w, y, borderColor);
        drawHLine(x, x + w, y + h, borderColor);
        drawVLine(x, y, y + h, borderColor);
        drawVLine(x + w, y, y + h, borderColor);
    }

    void drawString(String text, int x, int y, int maxChars, Vec4 color) {
        spriteRenderer.push(
            text.substring(0, Math.min(text.length(), maxChars)), 
            columns, charW, charH, scale, 0, x * scale, y * scale, color, color
        );
    }

    public void begin() {
        visible.clear();
        x = 0;
        y = 0;
        sx = 0;
        maxH = 0;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
        sx = x;
        maxH = 0;
    }

    public void moveRightOf(String key, int gap) {
        this.x = controls.get(key).x() + controls.get(key).w(this) + gap;
        this.y = controls.get(key).y();
        sx = x;
        maxH = 0;
    }

    public void addRow(int gap) {
        x = sx;
        y = y + maxH + gap;
        maxH = 0;
    }

    public void label(String key, String text, int maxChars, int gap) {
        UILabel label = (UILabel)controls.get(key);

        if(label == null) {
            controls.put(key, label = new UILabel(maxChars));
        }
        label.setPosition(x + gap, y);
        label.setText(text);

        x = x + gap + label.w(this);

        maxH = Math.max(maxH, label.h(this));

        visible.add(label);
    }

    public String text(String key, String text, int maxChars, boolean updateText, int gap) {
        UIText t = (UIText)controls.get(key);

        if(t == null) {
            controls.put(key, t = new UIText(maxChars));
        }
        t.setPosition(x + gap, y);
        if(updateText) {
            t.setText(text);
        }

        x = x + gap + t.w(this);

        maxH = Math.max(maxH, t.h(this));

        visible.add(t);

        return t.changed();
    }

    public boolean button(String key, String text, boolean selected, int gap) {
        UIButton button = (UIButton)controls.get(key);

        if(button == null) {
            controls.put(key, button = new UIButton(text));
        }
        button.setPosition(x + gap, y);
        x = x + gap + button.w(this);

        visible.add(button);
        button.setSelected(selected);

        maxH = Math.max(maxH, button.h(this));

        return button.clicked();
    }

    public float slider(String key, int width, float value, boolean setValue, int gap) {
        UISlider slider = (UISlider)controls.get(key);

        if(slider == null) {
            controls.put(key, slider = new UISlider(width));
        }
        slider.setPosition(x + gap, y);
        x = x + gap + slider.w(this);

        visible.add(slider);

        if(setValue) {
            slider.setValue(value);
        }
        maxH = Math.max(maxH, slider.h(this));

        return slider.value();
    }

    public int list(String key, Vector<Object> items, int rows, int cols, int selected, int gap) {
        UIList list = (UIList)controls.get(key);

        if(list == null) {
            controls.put(key, list = new UIList(rows, cols));
        }
        list.setPosition(x + gap, y);
        x = x + gap + list.w(this);

        list.setItems(items);
        list.setSelected(selected);

        visible.add(list);

        maxH = Math.max(maxH, list.h(this));

        return list.getSelected();
    }

    public void end(Game game) throws Exception {
        int x = game.getMouseX();
        int y = game.getMouseY();
        boolean hasFocus = false;

        spriteRenderer.setProjection(game.getFrameWidth(), game.getFrameHeight());
        spriteRenderer.begin(font);
        for(UIControl control : visible) {
            control.draw(this);
            if(control == focus) {
                hasFocus = true;
            }
        }
        spriteRenderer.end();

        if(!hasFocus) {
            focus = null;
        }

        boolean wasDown = down;

        if(game.isMouseButtonDown(0)) {
            if(!down) {
                down = true;
                for(UIControl control : visible) {;
                    if(control.hitTest(this, x, y)) {
                        focus = control;
                    }
                }
            }
        } else {
            if(focus != null && down) {
                focus.mouseUp(this, x, y);
            }
            down = false;
        }
        if(focus != null) {
            if(down && !wasDown) {
                focus.mouseDown(this, x, y);
            }
            focus.mouseMoved(this, x, y);
            focus.update(null);
        }
    }
}
