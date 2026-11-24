package org.game;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

public class GameSystem {

    private static final int ZOOM = 0;
    private static final int ROT = 1;
    private static final int PANXZ = 2;
    private static final int PANY = 3;
    private static final int SEL = 4;
    private static final int MOVXZ = 5;
    private static final int MOVY = 6;
    private static final int ROTX = 7;
    private static final int ROTY = 8;
    private static final int ROTZ = 9;

    private static final String[] MODES = new String[] {
        "ZM", "RT", "PXZ", "PY", "SEL", "MXZ", "MY", "RX", "RY", "RZ"
    };
    private static final int[] SNAP = new int[] {
        1, 2, 4, 8, 16, 32, 64
    };
    
    public final Game game;
    public final Vec3 eye = new Vec3(100, 100, 100);
    public final Vec3 target = new Vec3();
    public final Vec3 up = new Vec3(0, 1, 0);
    public final Mat4 view = new Mat4();
    public final Mat4 projection = new Mat4();
    public final Frustum frustum = new Frustum();
    public float fovDegrees = 60;
    public float zNear = 1;
    public float zFar = 25000;
    public final Vec4 backgroundColor = new Vec4(UIColors.BACKGROUND);
   
    private Hashtable<String, GameComponent> factories = new Hashtable<>();
    private Vector<Object> factoryNames = new Vector<>();
    private Vector<GameNode> nodes = new Vector<>();
    private Vector<GameComponent> components = new Vector<>();
    private BasicRenderer renderer;
    private SpriteRenderer spriteRenderer;
    private UI ui;
    private Vector<GameLight> lights = new Vector<>();
    private Mat4 m = new Mat4();
    private boolean inDesign;
    private Vector<Object> sceneNames = new Vector<>();
    private boolean uiEnabled;
    private int selectedScene = -1;
    private int sceneIndex = -1;
    private int selectedFactory = -1;
    private int factoryIndex = -1;
    private int mode = 0;
    private int snap = 0;
    private int iNode = -1;
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private float[] time = new float[1];
    private boolean down = false;
    private boolean captured = false;
    private boolean updateValueText = false;

    public GameSystem(String sceneName, boolean inDesign, boolean uiEnabled, GameComponent ... factories) throws Exception {
        this.inDesign = inDesign;

        game = Game.getInstance();

        ui = createUI(getFont());

        if(!uiEnabled) {
            this.inDesign = false;
        }
        this.uiEnabled = uiEnabled;

        for(GameComponent factory : factories) {
            String name = factory.toString();

            this.factories.put(name, factory);
            factoryNames.add(name);
        }

        File[] list = game.getResources().getRoot().listFiles();

        if(list != null) {
            for(File file : list) {
                if(file.getName().endsWith(".scene")) {
                    sceneNames.add(file.getName());
                }
            }
            sceneNames.sort((a, b) -> ((String)a).compareTo((String)b));
            selectedScene = sceneName.indexOf(sceneName);
            sceneIndex = selectedScene;
            load();
        }
    }

    public final int x() {
        if(inDesign) {
            return 185 * game.getScale();
        }
        return 0;
    }

    public final int y() {
        if(inDesign) {
            return 10 * game.getScale();
        }
        return 0;
    }

    public final int w() {
        if(inDesign) {
            return game.getFrameWidth() - 195 * game.getScale();
        }
        return game.getFrameWidth();
    }

    public final int h() {
        if(inDesign) {
            return game.getFrameHeight() - 60 * game.getScale();
        }
        return game.getFrameHeight();
    }

    public final boolean inDesign() {
        return inDesign;
    }

    public final String getSceneName() {
        if(selectedScene >= 0) {
            return (String)sceneNames.get(selectedScene);
        }
        return null;
    }

    public final int getCount() {
        return components.size();
    }

    public final GameComponent getComponent(int i) {
        return components.get(i);
    }

    public final GameNode getNode(int i) {
        return nodes.get(i);
    }

    public final int getSelectedNode() {
        return iNode;
    }

    protected BasicRenderer createRenderer() throws Exception {
        return new BasicRenderer();
    }

    protected UI createUI(Texture font) {
        return new UI(spriteRenderer, font, 16, 10, 12, 14, 5);  
    }

    protected Texture getFont() throws Exception {
        return game.getResources().load(new File("sprites.png"));
    }

    protected Texture postProcess(Texture texture) throws Exception {
        return texture;
    }

    public final void update() throws Exception {
        lights.clear();

        for(int i = 0; i != components.size(); i++) {
            GameComponent component = components.get(i);
            GameNode node = nodes.get(i);

            if(!inDesign) {
                File file;

                if((file = component.loadFile(this, node)) != null) {
                    load(file);
                    break;
                }
            }
        }

        for(int i = 0; i != components.size(); i++) {
            GameComponent component = components.get(i);
            GameNode node = nodes.get(i);

            component.update(this, node);

            for(int j = 0; j != 64; j++) {
                GameLight light = component.getLight(this, node, j);

                if(light != null) {
                    lights.add(light);
                } else {
                    break;
                }
            }
        }

        lights.sort((a, b) -> {
            return Float.compare(a.position.distance(target), b.position.distance(target));
        });

        calcTransform();

        int lightCount = Math.min(BasicRenderer.MAX_LIGHTS, lights.size());

        renderer.begin(w(), h(), backgroundColor);
        renderer.setProjection(projection);
        renderer.setView(view);
        renderer.setLightCount(lightCount);
        for(int i = 0; i != lightCount; i++) {
            GameLight light = lights.get(i);
            
            renderer.setLightPosition(i, light.position);
            renderer.setLightColor(i, light.color);
            renderer.setLightRadius(i, light.radius);
        }
        GFX.setDepthState(DepthState.NONE);
        for(int i = 0; i != components.size(); i++ ) {
            components.get(i).render(this, nodes.get(i), renderer, RenderPass.BACKGROUND);
        }
        GFX.setDepthState(DepthState.READWRITE);
        GFX.setBlendState(BlendState.OPAQUE);
        GFX.setCullState(CullState.BACK);
        for(int i = 0; i != components.size(); i++ ) {
            components.get(i).render(this, nodes.get(i), renderer, RenderPass.SOLID);
        }
        GFX.setDepthState(DepthState.READONLY);
        GFX.setCullState(CullState.BACK);
        for(int i = 0; i != components.size(); i++ ) {
            components.get(i).render(this, nodes.get(i), renderer, RenderPass.TRANSPARENT);
        }
        renderer.end();

        Texture texture = postProcess(renderer.getRenderTarget().getTexture(0));

        GFX.clear(UIColors.BACKGROUND);
        GFX.setViewport(x(), y(), w(), h());

        GFX.setDepthState(DepthState.NONE);
        GFX.setCullState(CullState.NONE);
        GFX.setBlendState(BlendState.ALPHA);
        spriteRenderer.setProjection(w(), h());
        spriteRenderer.begin(texture);
        spriteRenderer.push(
            0, 0, texture.width, texture.height,
            0, 0, w(), h(), 1, 1, 1, 1, 1, 1, 1, 1, true
        );
        spriteRenderer.end();

        GFX.setViewport(0, 0, game.getFrameWidth(), game.getFrameHeight());

        if(inDesign) {
            boolean resetS = true;
            boolean resetF = true;
            int i;

            ui.begin();
            ui.moveTo(10, 10);
            if(ui.button("save", "SAVE", false, 0)) {
                save();
            }
            if(ui.button("play", "PLAY", false, 5)) {
                if(inDesign) {
                    inDesign = false;
                    load(new File(game.getResources().getRoot(), getSceneName()));
                }
            }
            for(i = 0; i != MODES.length; i++) {
                if(ui.button("mode" + i, MODES[i], i == mode, (i == 0) ? 20 : 5)) {
                    mode = i;
                }
            }
            if(ui.button("swap", "SI", game.isSwapOne(),20)) {
                game.toggleSwapInterval();;
            }
            if(ui.button("fullscreen", "FS", game.isFullscreen(), 5)) {
                game.toggleFullscreen();
            }
            if(iNode != -1) {
                ui.label("selectedLabel", components.get(iNode).toString(), 25,10);
            }
            ui.addRow(10);
            i = ui.list("sceneList", sceneNames, 4, 14, sceneIndex, 0);
            if(i != selectedScene && i != -1) {
                selectedScene = i;
                sceneIndex = i;
                resetS = false;
                load();
            }
            ui.addRow(5);
            i = ui.list("fList", factoryNames, 6, 14, factoryIndex, 0);
            if(i != selectedFactory && i != -1) {
                selectedFactory = i;
                factoryIndex = i;
                resetF = false;
            }
            ui.addRow(5);
            for(i = 0; i != SNAP.length; i++) {
                if(i == 4) {
                    ui.addRow(5);
                }
                if(ui.button("snap" + i, (SNAP[i] < 16) ? SNAP[i] + " " : "" + SNAP[i], snap == i, (i == 0 || i == 4) ? 0 : 5)) {
                    snap = i;
                }
            }
            ui.addRow(5);
            if(ui.button("+", "+ ", false, 0)) {
                if(selectedFactory != -1) {
                    GameComponent component = factories.get(factoryNames.get(selectedFactory));
                    GameNode node = new GameNode();

                    node.position.set(target);
                    iNode = nodes.size();
                    component.init(this, node);
                    components.add(component);
                    nodes.add(node);

                    updateValueText = true;
                }
            }
            if(iNode != -1) {
                if(ui.button("-", "- ", false, 5)) {
                    components.remove(iNode);
                    nodes.remove(iNode);
                    iNode = -1;
                }
                if(ui.button("zp", "ZP", false, 5)) {
                    nodes.get(iNode).position.zero();
                }
                ui.addRow(5);
                if(ui.button("zr", "ZR", false, 0)) {
                    nodes.get(iNode).r.set(1, 0, 0);
                    nodes.get(iNode).u.set(0, 1, 0);
                    nodes.get(iNode).f.set(0, 0, 1);
                }
                if(ui.button("2t", "2T", false, 5)) {
                    nodes.get(iNode).position.set(target);
                }
                if(ui.button("t2", "T2", false, 5)) {
                    target.set(nodes.get(iNode).position);
                }
                ui.addRow(5);
                if(ui.button("rx", "RX", false, 0)) {
                    nodes.get(iNode).rotate(0, 45);
                }
                if(ui.button("ry", "RY", false, 5)) {
                    nodes.get(iNode).rotate(1, 45);
                }
                if(ui.button("rz", "RZ", false, 5)) {
                    nodes.get(iNode).rotate(2, 45);
                }
            }
            if(ui.button("ic", "IC", false, 5)) {
                eye.set(100, 100, 100);
                target.zero();
                up.set(0, 1, 0);
            }
            if(iNode != -1) {
                GameNode node = nodes.get(iNode);

                ui.addRow(5);
                ui.label("valueLabel", "value", 5, 0);
                ui.addRow(5);

                String r;

                if((r = ui.text("valueText", node.value, 15, updateValueText, 0)) != null) {
                    node.value = r;
                    components.get(iNode).parseValue(this, node);
                }
            }
            ui.addRow(5);
            updateValueText = false;
            if(resetS) {
                sceneIndex = -2;
            }
            if(resetF) {
                factoryIndex = -2;
            }
            ui.end(game);
        } 

        GFX.setViewport(x(), y(), w(), h());
        spriteRenderer.setProjection(w(), h());
        for(int i = 0; i != components.size(); i++) {
            components.get(i).render(this, nodes.get(i), spriteRenderer, renderer);
        }
        GFX.setViewport(0, 0, game.getFrameWidth(), game.getFrameHeight());

        if(!inDesign) {
            if(game.isKeyDown(Keys.KEY_ESCAPE)) {
                if(uiEnabled) {
                    inDesign = true;
                    load(new File(game.getResources().getRoot(), getSceneName()));
                } else {
                    game.getResources().destroy();
                    System.exit(0);
                }
            }
        }

        if(inDesign) {
            if(game.isMouseButtonDown(0)) {
                int s = game.getScale();
                int x = game.getMouseX() * s;
                int y = game.getMouseY() * s;
                int x1 = x();
                int y1 = game.getFrameHeight() - y() - h();
                int x2 = x() + w();
                int y2 =  game.getFrameHeight() - y();
                
                if(!captured) {
                    if(!down) {
                        captured = x >= x1 && x <= x2 && y >= y1 && y <= y2;
                    }
                }
                if(captured) {
                    if(mode == ZOOM) {
                        CameraHelper.zoom(eye, target, -game.getDeltaY());
                    } else if(mode == ROT) {
                        CameraHelper.rotateAroundTarget(eye, target, up, game.getDeltaX() * 0.025f, -game.getDeltaY() * 0.025f);
                    } else if(mode == PANXZ) {
                        CameraHelper.move(target, eye, target, game.getDeltaX(), -game.getDeltaY());
                    } else if(mode == PANY) {
                        CameraHelper.move(target, eye, target, -game.getDeltaY());
                    } else if(mode == SEL) {
                        if(!down) {
                            int vw = x2 - x1;
                            int vh = y2 - y1;

                            x -= x1;
                            y -= y1;
                            y = vh - y - 1;

                            CameraHelper.unproject(x, y, 0, 0, 0, vw, vh, projection, view, origin);
                            CameraHelper.unproject(x, y, 1, 0, 0, vw, vh, projection, view, direction).sub(origin).normalize();

                            time[0] = Float.MAX_VALUE;

                            iNode = -1;
                            for(int i = 0; i != nodes.size(); i++) {
                                GameComponent component = components.get(i);
                                GameNode node = nodes.get(i);

                                if(component.pick(this, node, origin, direction, time)) {
                                    iNode = i;
                                    updateValueText = true;
                                }
                            }
                        }
                    } else if(iNode >= 0) {
                        GameNode node = nodes.get(iNode);

                        if(mode == MOVXZ) {
                            CameraHelper.move(node.position, eye, target, -game.getDeltaX(), game.getDeltaY());
                        } else if(mode == MOVY) {
                            CameraHelper.move(node.position, eye, target, game.getDeltaY());
                        } else if(mode == ROTX) {
                            node.rotate(0, game.getDeltaX());
                        } else if(mode == ROTY) {
                            node.rotate(1, game.getDeltaX());
                        } else if(mode == ROTZ) {
                            node.rotate(2, game.getDeltaX());
                        }
                    }
                }
                down = true;
            } else {
                if(down) {
                    int s = SNAP[snap];

                    if(iNode >= 0 && (mode == MOVXZ || mode == MOVY)) {
                        GameNode node = nodes.get(iNode);

                        node.position.x = Math.round(node.position.x / s) * s;
                        node.position.y = Math.round(node.position.y / s) * s;
                        node.position.z = Math.round(node.position.z / s) * s;
                    } else if(mode == PANXZ || mode == PANY) {
                        float dx = eye.x - target.x;
                        float dy = eye.y - target.y;
                        float dz = eye.z - target.z;

                        target.x = Math.round(target.x / s) * s;
                        target.y = Math.round(target.y / s) * s;
                        target.z = Math.round(target.z / s) * s;
                        target.add(dx, dy, dz, eye);
                    }
                }
                down = false;
                captured = false;
            }
        }
        lights.clear();
    }

    private final void save() throws Exception {
        File file = new File(game.getResources().getRoot(), getSceneName());
        String text = "";

        Log.getInstance().put(1, "saving '" + file + "' ...");

        for(int i = 0; i != components.size(); i++) {
            GameNode node = nodes.get(i);

            text += "# name x y z rx ry rz ux uy uz fx fy fz\n";
            text += components.get(i).toString() + " ";
            text += node.position.x + " ";
            text += node.position.y + " ";
            text += node.position.z + " ";
            text += node.r.x + " ";
            text += node.r.y + " ";
            text += node.r.z + " ";
            text += node.u.x + " ";
            text += node.u.y + " ";
            text += node.u.z + " ";
            text += node.f.x + " ";
            text += node.f.y + " ";
            text += node.f.z + " ";
            if(node.value != null) {
                text += node.value;
            }
            text += "\n";
        }
        IOHelper.writeAllBytes(text.getBytes(), file);
    }

    private final void load() throws Exception {
        load(new File(game.getResources().getRoot(), getSceneName()));
    }

    private final void load(File sceneFile) throws Exception {
        Log.getInstance().put(1, "loading '" + sceneFile + "' ...");

        String[] lines = new String(IOHelper.readAllBytes(sceneFile)).split("\\n+");

        for(int i = 0; i != factoryNames.size(); i++) {
            factories.get(factoryNames.get(i)).finish();
        }

        game.setMouseGrabbed(false);

        nodes.clear();
        components.clear();

        game.getResources().clear();
        
        renderer = game.getResources().manage(createRenderer());
        spriteRenderer = game.getResources().manage(new SpriteRenderer());
        ui.setFont(getFont());
        ui.setSpriteRenderer(spriteRenderer);

        eye.set(100, 100, 100);
        target.zero();
        up.set(0, 1, 0);
        fovDegrees = 60;
        zNear = 1;
        zFar = 25000;
        calcTransform();

        mode = 0;
        snap = 0;

        selectedFactory = this.factoryNames.indexOf(factoryNames.get(0).toString());
        factoryIndex = selectedFactory;
        iNode = -1;

        for(String line : lines) {
            String tLine = line.trim();
            
            if(!tLine.startsWith("#") && !tLine.isEmpty()) {
                String[] tokens = tLine.split("\\s+");

                if(tokens.length >= 13) {
                    try {
                        GameNode node = new GameNode();
                        GameComponent component = factories.get(tokens[0]);

                        node.position.x = Float.parseFloat(tokens[1]);
                        node.position.y = Float.parseFloat(tokens[2]);
                        node.position.z = Float.parseFloat(tokens[3]);
                        node.r.x = Float.parseFloat(tokens[4]);
                        node.r.y = Float.parseFloat(tokens[5]);
                        node.r.z = Float.parseFloat(tokens[6]);
                        node.u.x = Float.parseFloat(tokens[7]);
                        node.u.y = Float.parseFloat(tokens[8]);
                        node.u.z = Float.parseFloat(tokens[9]);
                        node.f.x = Float.parseFloat(tokens[10]);
                        node.f.y = Float.parseFloat(tokens[11]);
                        node.f.z = Float.parseFloat(tokens[12]);

                        if(tokens.length > 13) {
                            node.value = tokens[13];
                            component.parseValue(this, node);
                        }

                        component.init(this, node);
                        nodes.add(node);
                        components.add(component);
                    } catch(Exception ex) {
                        Log.getInstance().put(0, ex);
                    }
                }
            }
        }
    }

    private final void calcTransform() {
        projection.identity().perspective((float)Math.toRadians(fovDegrees), w() / (float)h(), zNear, zFar);
        view.identity().lookAt(eye, target, up);

        frustum.set(m.set(projection).mul(view));
     }
}
