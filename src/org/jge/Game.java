package org.jge;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Dimension;

public class Game {

    public static interface GameLoop {
        
        void init() throws Exception;

        void render() throws Exception;
    }

    private static Game instance = null;
    
    public static Game getInstance() {
        return instance;
    }

    public static GL2 getGL() {
        return (GL2)getInstance().getCanvas().getGL();
    }

    private class Listener implements MouseListener, MouseMotionListener, KeyListener, GLEventListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastX = mouseX = e.getX();
            lastY = mouseY = e.getY();
            dX = 0;
            dY = 0;
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = true;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = true;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = true;
            }
            canvas.requestFocus();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = false;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = false;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = false;
            }
            dX = 0;
            dY = 0;
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            try {
                loop.render();
                GFX.checkError("display");
                tick();
                dX = 0;
                dY = 0;
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
            try {
                System.out.println(Resource.getInstances() + " instance(s)");
                resources.destroy();
                System.out.println(Resource.getInstances() + " instance(s)");
                GFX.checkError("dispose");
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            } 
        }

        @Override
        public void init(GLAutoDrawable drawable) {
            try {
                if(fixedFrameRate) {
                    getGL().setSwapInterval(1);
                }
                lineRenderer = resources.manage(new LineRenderer());
                colorRenderer = resources.manage(new ColorRenderer());
                lightRenderer = resources.manage(new LightRenderer());
                spriteRenderer = resources.manage(new SpriteRenderer());
                loop.init();
                GFX.checkError("init");
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
                System.exit(0);
            }
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
            GL2 gl = (GL2)drawable.getGL();

            gl.glViewport(x, y, w, h);
        }
    }

    private GameLoop loop;
    private ResourceManager resources = new ResourceManager();
    private AssetManager assets;
    private SpriteRenderer spriteRenderer = null;
    private LineRenderer lineRenderer = null;
    private LightRenderer lightRenderer = null;
    private ColorRenderer colorRenderer = null;
    private SceneRenderer sceneRenderer = new SceneRenderer();
    private int mouseX = 0;
    private int mouseY = 0;
    private int dX = 0;
    private int dY = 0;
    private int lastX = 0;
    private int lastY = 0;
    private boolean[] buttonState = new boolean[]{false, false, false};
    private boolean[] keyState = new boolean[256];
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private GLCanvas canvas = null;
    private boolean fixedFrameRate;

    public Game(int w, int h, boolean fixedFrameRate, GameLoop loop) {
        instance = this;

        this.fixedFrameRate = fixedFrameRate;

        assets = resources.manage(new AssetManager());

        GLProfile profile = GLProfile.getGL2ES1();
        GLCapabilities caps = new GLCapabilities(profile);

        caps.setRedBits(8);
        caps.setGreenBits(8);
        caps.setBlueBits(8);
        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setStencilBits(8);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(new Listener());
        canvas.addKeyListener(new Listener());
        canvas.addMouseListener(new Listener());
        canvas.addMouseMotionListener(new Listener());
        canvas.setPreferredSize(new Dimension(w, h));
        this.loop = loop;
        for (int i = 0; i != keyState.length; i++) {
            keyState[i] = false;
        }
        canvas.setFocusable(true);
        canvas.requestFocus();
    }

    public GLCanvas getCanvas() {
        return canvas;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public SceneRenderer getSceneRenderer() {
        return sceneRenderer;
    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public ColorRenderer getColorRenderer() {
        return colorRenderer;
    }

    public LightRenderer getLightRenderer() {
        return lightRenderer;
    }

    public SpriteRenderer getSpriteRenderer() {
        return spriteRenderer;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public int dX() {
        return dX;
    }

    public int dY() {
        return dY;
    }

    public boolean buttonDown(int i) {
        return buttonState[i];
    }

    public boolean keyDown(int i) {
        return keyState[i];
    }

    public int w() {
        return canvas.getWidth();
    }

    public int h() {
        return canvas.getHeight();
    }

    public float aspectRatio() {
        return w() / (float) h();
    }

    public float totalTime() {
        return (float) totalTime;
    }

    public float elapsedTime() {
        return (float) elapsedTime;
    }

    public int frameRate() {
        return fps;
    }

    public void resetTimer() {
        lastTime = System.nanoTime() / 1000000000.0;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    void tick() {
        double nowTime = System.nanoTime() / 1000000000.0;
        elapsedTime = nowTime - lastTime;
        lastTime = nowTime;
        seconds += elapsedTime;
        totalTime += elapsedTime;
        frames++;
        if (seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }
    }

    public void start() {
        canvas.setAnimator(new Animator(canvas));
        canvas.getAnimator().start();
    }
}
