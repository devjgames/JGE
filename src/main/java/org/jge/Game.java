package org.jge;

import java.util.Hashtable;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Game {

    private static Game instance = null;

    public static Game getInstance() {
        return instance;
    }
    
    public final ResourceManager resources = new ResourceManager();
    public final long window;
    public final AssetManager assets;
    public final SceneRenderer sceneRenderer = new SceneRenderer();

    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private int dX = 0;
    private int dY = 0;
    private int lastX = 0;
    private int lastY = 0;
    private double[] mouseX = new double[1];
    private double[] mouseY = new double[1];
    private int[] width = new int[1];
    private int[] height = new int[1];
    private int[] frameWidth = new int[1];
    private int[] frameHeight = new int[1];
    private boolean sync = true;
    private boolean continuousMouse = false;
    private boolean fullscreen = false;
    private GLFWVidMode mode;
    private int[] windowX = new int[1];
    private int[] WindowY = new int[1];
    private int lastWidth = 0;
    private int lastHeight = 0; 
    private final Hashtable<String, Renderer> renders = new Hashtable<>();

    public Game(int width, int height, boolean resizable) throws Exception {
        instance = this;
        
        GLFW.glfwSetErrorCallback(new GLFWErrorCallbackI() {

            @Override
            public void invoke(int error, long description) {
                System.out.println("GLFW error:" + error);
            } 
        });
        if(!GLFW.glfwInit()) {
            throw new Exception("failed to initialize GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, (resizable) ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(width, height, "JGE", 0, 0);
        if(window == 0) {
            GLFW.glfwTerminate();
            throw new Exception("failed to create GLFW window");
        }
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(1);

        assets = resources.manage(new AssetManager());

        mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        GLFW.glfwPollEvents();

        lastX = getMouseX();
        lastY = getMouseY();

        restTimer();
    }

    public float getTotalTime() {
        return (float)totalTime;
    }

    public float getElapsedTime() {
        return (float)elapsedTime;
    }

    public int getFrameRate() {
        return fps;
    }

    public int getDeltaX() {
        return dX;
    }

    public int getDeltaY() {
        return dY;
    }

    public int getMouseX() {
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);

        return (int)mouseX[0];
    }

    public int getMouseY() {
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);

        return (int)mouseY[0];
    }

    public boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public int getWindowWidth() {
        GLFW.glfwGetWindowSize(window, width, height);

        return width[0];
    }

    public int getWindowHeight() {
        GLFW.glfwGetWindowSize(window, width, height);

        return height[0];
    }

    public int getFrameWidth() {
        GLFW.glfwGetFramebufferSize(window, frameWidth, frameHeight);

        return frameWidth[0];
    }

    public int getFrameHeight() {
        GLFW.glfwGetFramebufferSize(window, frameWidth, frameHeight);

        return frameHeight[0];
    }

    public int getScale() {
        return getFrameWidth() / getWindowWidth();
    }

    public float getAspectRatio() {
        return getWindowWidth() / (float)getWindowHeight();
    }

    @SuppressWarnings("unchecked")
    public <T extends Renderer> T getRenderer(Class<? extends Renderer> cls) throws Exception {
        String key = cls.getName();

        if(!renders.containsKey(key)) {
            System.out.println("allocating renderer " + key + " ...");
            renders.put(key, resources.manage((Renderer)cls.getConstructors()[0].newInstance()));
        }
        return (T)renders.get(key);
    }

    public boolean isSyncEnabled() {
        return sync;
    }

    public void toggleSync() {
        sync = !sync;
        if(sync) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
    }

    public boolean isContinuousMouseEnabled() {
        return continuousMouse;
    }

    public void setContinuousMouseEnabled(boolean enabled) {
        continuousMouse = enabled;
        if(continuousMouse) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            GLFW.glfwPollEvents();
            dX = 0;
            dY = 0;
            GLFW.glfwSetCursorPos(window, getWindowWidth() / 2, getWindowHeight() / 2);
        } else {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        if(fullscreen) {
            GLFW.glfwGetWindowPos(window, windowX, WindowY);
            lastWidth = getWindowWidth();
            lastHeight = getWindowHeight();
            GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, windowX[0], WindowY[0], lastWidth, lastHeight, 0);
        }
    }

    public boolean nextFrame() {
        GLFW.glfwPollEvents();

        GL11.glViewport(0, 0, getFrameWidth(), getFrameHeight());

        return !GLFW.glfwWindowShouldClose(window);
    }

    public void swapBuffers() {
        GFX.checkError("Game.swapBuffers()");
        GLFW.glfwSwapBuffers(window);

        double now = System.nanoTime() / 1000000000.0;

        if(isContinuousMouseEnabled()) {
            int x = getWindowWidth() / 2;
            int y = getWindowHeight() / 2;

            dX = getMouseX() - x;
            dY = getMouseY() - y;

            GLFW.glfwSetCursorPos(window, x, y);
        } else {
            int x = getMouseX();
            int y = getMouseY();

            dX = x - lastX;
            dY = y - lastY;
            lastX = x;
            lastY = y;
        }

        elapsedTime = now - lastTime;
        lastTime = now;
        totalTime += elapsedTime;
        seconds += elapsedTime;
        frames++;
        if(seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }
    }

    public void restTimer() {
        lastTime = System.nanoTime() / 1000000000.0;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames  = 0;
        fps = 0;
    }

    public void destroy() throws Exception {
        System.out.println(Resource.getInstances() + " instance(s)");
        resources.destroy();
        System.out.println(Resource.getInstances() + " instance(s)");
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
