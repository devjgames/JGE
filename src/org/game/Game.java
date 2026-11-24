package org.game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Game {

    private static Game instance = null;

    public static Game getInstance() {
        return instance;
    }

    private long window = 0;
    private int[] windowX = new int[] { 0 };
    private int[] windowY = new int[] { 0 };
    private int[] windowWidth = new int[] { 0 };
    private int[] windowHeight = new int[] { 0 };
    private int[] frameWidth = new int[] { 0 };
    private int[] frameHeight = new int[] { 0 };
    private int lastWindowWidth = 0;
    private int lastWindowHeight = 0;
    private boolean isFullscreen = false;
    private double[] mouseX = new double[] { 0 };
    private double[] mouseY = new double[] { 0 };
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private int lastX = 0;
    private int lastY = 0;
    private int deltaX = 0;
    private int deltaY = 0;
    private int swapInterval = 1;
    private ResourceManager resources = new ResourceManager();
    private boolean mouseGrabbed = false;
    private SoundEngine soundEngine;

    public Game(int width, int height, String title, boolean resizable) throws Exception {
        instance = this;

        if(!GLFW.glfwInit()) {
            throw new Exception("failed to initialize glfw");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, (resizable) ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(width, height, title, 0, 0);

        if(window == 0) {
            GLFW.glfwTerminate();
            throw new Exception("failed to create glfw window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(swapInterval);

        Log.getInstance().put(1, GL11.glGetString(GL11.GL_VERSION));

        soundEngine = new SoundEngine();

        resetTimer();
    }

    public long getWindow() {
        return window;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
    }

    public String getTitle() {
        return GLFW.glfwGetWindowTitle(window);
    }

    public int getWindowWidth() {
        GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
        return windowWidth[0];
    }

    public int getWindowHeight() {
        GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
        return windowHeight[0];
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

    public int getMouseX() {
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);
        return (int)mouseX[0];
    }

    public int getMouseY() {
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);
        return (int)mouseY[0];
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public float getTotalTime() {
        return (float)totalTime;
    }

    public float getElapsedTime() {
        return (float)elapsedTime;
    }

    public int getFPS() {
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

    public boolean isSwapOne() {
        return swapInterval == 1;
    }

    public void toggleSwapInterval() {
        if(swapInterval == 1) {
            swapInterval = 0;
        } else {
            swapInterval = 1;
        }
        GLFW.glfwSwapInterval(swapInterval);
    }

    public void setMouseGrabbed(boolean grabbed) {
        mouseGrabbed = grabbed;
        if(mouseGrabbed) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        } else {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    public boolean beginFrame() {
        if(!GLFW.glfwWindowShouldClose(window)) {
            GFX.setViewport(0, 0, getFrameWidth(), getFrameHeight());
            return true;
        }
        return false;
    }

    public void endFrame() {
        GFX.checkError("endFrame()");

        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();

        double now = System.nanoTime() / 1000000000.0;

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

        int x = getMouseX();
        int y = getMouseY();
        
        deltaX = lastX - x;
        deltaY = lastY - y;

        lastX = x;
        lastY = y;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void toggleFullscreen() {
        isFullscreen = !isFullscreen;

        if(isFullscreen) {
            GLFW.glfwGetWindowPos(window, windowX, windowY);
            lastWindowWidth = getWindowWidth();
            lastWindowHeight = getWindowHeight();

            long monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);

            GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, windowX[0], windowY[0], lastWindowWidth, lastWindowHeight, -1);
        }
    }

    public void destroy() throws Exception {
        Log.getInstance().put(1, Resource.getInstances() + " resource(s)");
        soundEngine.destroy();
        resources.destroy();
        Log.getInstance().put(1, Resource.getInstances() + " resource(s)");
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}