package org.jge;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class GameEditor implements org.jge.Game.GameLoop {

    private static final int ROT = 0;
    private static final int ZOOM = 1;
    private static final int PANXZ = 2;
    private static final int PANY = 3;
    private static final int SEL = 4;
    private static final int MOVXZ = 5;
    private static final int MOVY = 6;
    private static final int RX = 7;
    private static final int RY = 8;
    private static final int RZ = 9;
    private static final int SCALE = 10;
    private static final int COMPONENT = 11;

    private static class NodeComponentFactory {

        public final Class<?> cls;

        public NodeComponentFactory(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public String toString() {
            return cls.getSimpleName();
        }
    }

    private static class TextAreaStream extends OutputStream {

        public static boolean consolePaused = false;

        private JTextArea textArea;
        private final File logFile = IO.file("log.txt");

        public TextAreaStream(JTextArea textArea) {
            this.textArea = textArea;

            if(logFile.exists()) {
                logFile.delete();
            }
            consolePaused = false;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if(consolePaused) {
                return;
            }
            String s = new String(b, off, len);

            textArea.append(s);
            textArea.setCaretPosition(textArea.getText().length());

            try {
                IO.appendAllBytes(s.getBytes(), logFile);
            } catch(Exception ex) {
            }
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }
    }

    private static class EditorPane {

        public static final String PFX = "org.jge.EditorPane.KEY.PFX";
        
        private JScrollPane scrollPane;
        private JPanel editorPanel;
        private JPanel editorContainerPanel;

        public EditorPane(int width) {
            BoxLayout box;

            editorContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            editorPanel = new JPanel();
            box = new BoxLayout(editorPanel, BoxLayout.Y_AXIS);
            editorPanel.setLayout(box);
            editorContainerPanel.add(editorPanel);

            scrollPane = new JScrollPane(editorContainerPanel);
            scrollPane.setPreferredSize(new Dimension(width, 50));
        }

        public JScrollPane getScrollPane() {
            return scrollPane;
        }

        public JPanel getEditorPanel() {
            return editorPanel;
        }

        public void begin() {
            editorPanel.removeAll();
        }

        public void end() {
            editorContainerPanel.getParent().validate();
        }

        @SuppressWarnings("unchecked")
        public void addFields(Object o, JTree tree, DefaultTreeModel model, Class<?> nodeCls) {
            Class<? extends Object> cls = o.getClass();
            Field[] fields = cls.getFields();

            for(Field field : fields) {
                Class<? extends Object> type = field.getType();
                String name = field.getName();
                int m = field.getModifiers();
                boolean hidden = field.getAnnotationsByType(Hidden.class).length != 0;

                if(hidden) {
                    continue;
                }

                if(
                    ((int.class.isAssignableFrom(type) ||
                    float.class.isAssignableFrom(type) ||
                    String.class.isAssignableFrom(type)) && !Modifier.isStatic(m) && !Modifier.isFinal(m)) ||
                    ((Vector2f.class.isAssignableFrom(type) ||
                    Vector3f.class.isAssignableFrom(type) ||
                    Vector4f.class.isAssignableFrom(type)) && !Modifier.isStatic(m))
                ) {
                    try {
                        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                        boolean multiLine = field.getAnnotationsByType(MultiLine.class).length != 0;

                        if(String.class.isAssignableFrom(type) && multiLine) {
                            JButton editTextButton = new JButton(new AbstractAction(name + " ...") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    JDialog dialog = new JDialog();
                                    JButton button = (JButton)e.getSource();
                                    String fname = (String)button.getClientProperty(PFX + ".NAME");
                                    Object fo = button.getClientProperty(PFX + ".OBJECT");

                                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                    dialog.setResizable(true);
                                    dialog.setModal(true);
                                    dialog.setTitle(fname);
                                    dialog.setLayout(new BorderLayout());

                                    JTextArea textArea = new JTextArea();
                                    JScrollPane scrollPane = new JScrollPane(textArea);

                                    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                                    try {
                                        textArea.setText((String)fo.getClass().getField(fname).get(fo));
                                    } catch(Exception ex) {
                                        ex.printStackTrace(System.out);
                                    }
                                    scrollPane.setPreferredSize(new Dimension(400, 400));

                                    dialog.add(scrollPane, BorderLayout.CENTER);

                                    JButton saveButton = new JButton(new AbstractAction("Save") {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            JButton button = (JButton)e.getSource();
                                            String fname = (String)button.getClientProperty(PFX + ".NAME");
                                            Object fo = button.getClientProperty(PFX + ".OBJECT");
                                            JTextArea textArea = (JTextArea)button.getClientProperty(PFX + ".TEXT");

                                            try {
                                                fo.getClass().getField(fname).set(fo, textArea.getText());
                                            } catch(Exception ex) {
                                                ex.printStackTrace(System.out);
                                            }
                                        }
                                    });

                                    saveButton.putClientProperty(PFX + ".NAME", fname);
                                    saveButton.putClientProperty(PFX + ".OBJECT", fo);
                                    saveButton.putClientProperty(PFX + ".TEXT", textArea);

                                    JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                    
                                    flow.add(saveButton);
                                    dialog.add(flow, BorderLayout.SOUTH);

                                    dialog.pack();
                                    dialog.setVisible(true);
                                }
                            });
                            editTextButton.putClientProperty(PFX + ".NAME", name);
                            editTextButton.putClientProperty(PFX + ".OBJECT", o);
                            flowPanel.add(editTextButton);
                        } else {
                            JTextField textField = new JTextField(Utils.toString(o, name), 10);

                            textField.putClientProperty(PFX + ".NAME", name);
                            textField.putClientProperty(PFX + ".OBJECT", o);
                            flowPanel.add(textField);
                            flowPanel.add(new JLabel(name, JLabel.LEFT));

                            textField.addKeyListener(new KeyAdapter() {
                                @Override
                                public void keyReleased(KeyEvent e) {
                                    try {
                                        JTextField field = (JTextField)e.getSource();
                                        String fname = (String)field.getClientProperty(PFX + ".NAME");
                                        Object fo = field.getClientProperty(PFX + ".OBJECT");
        
                                        Utils.parse(fo, fname, field.getText());

                                        if(fo.getClass().isAssignableFrom(nodeCls) && fname.equals("name")) {
                                            TreeModelEvent tme = new TreeModelEvent(model, tree.getSelectionPath());
                                            TreeModelListener[] listeners = model.getTreeModelListeners();
                                            
                                            for(TreeModelListener l : listeners) {
                                                l.treeNodesChanged(tme);
                                            }
                                        }
                                    } catch(Exception ex) {
                                    }
                                }
                            });
                        }
                        editorPanel.add(flowPanel);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                } else if(boolean.class.isAssignableFrom(type) && !Modifier.isStatic(m) && !Modifier.isFinal(m)) {
                    try {
                        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                        JCheckBox checkBox = new JCheckBox(name, (Boolean)field.get(o));

                        checkBox.putClientProperty(PFX + ".NAME", name);
                        checkBox.putClientProperty(PFX + ".OBJECT", o);

                        flowPanel.add(checkBox);
                        editorPanel.add(flowPanel);

                        checkBox.addItemListener((e) -> {
                            JCheckBox cb = (JCheckBox)e.getSource();
                            String fname = (String)cb.getClientProperty(PFX + ".NAME");
                            Object fo = cb.getClientProperty(PFX + ".OBJECT");

                            try {
                                Utils.parse(fo, fname, (cb.isSelected()) ? "true" : "false");
                            } catch(Exception ex) {
                                ex.printStackTrace(System.out);
                            }
                        });
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                } else if(Button.class.isAssignableFrom(type) && !Modifier.isStatic(m)) {
                    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                    JButton button = new JButton(new AbstractAction(name) {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JButton button = (JButton)e.getSource();
                            String fname = (String)button.getClientProperty(PFX + ".NAME");
                            Object fo = button.getClientProperty(PFX + ".OBJECT");

                            try {
                                Button b = (Button)fo.getClass().getField(fname).get(fo);

                                b.onClick();
                            } catch(Exception ex) {
                                ex.printStackTrace(System.out);
                            }
                        };
                    });
                
                    button.putClientProperty(PFX + ".NAME", name);
                    button.putClientProperty(PFX + ".OBJECT", o);
                    flowPanel.add(button);
                    editorPanel.add(flowPanel);
                } else if(type.isEnum() && !Modifier.isStatic(m) && !Modifier.isFinal(m)) {
                    try {
                        Object[] values = type.getEnumConstants();
                        String item = Utils.toString(o, name);

                        if(field.getAnnotationsByType(EnumRadioButtons.class).length != 0) {
                            ButtonGroup group = new ButtonGroup();
                            JRadioButton selected = null;
                            JLabel label = new JLabel(name);
                            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

                            labelPanel.add(label);
                            editorPanel.add(labelPanel);
                            for(Object v : values) {
                                JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                                JRadioButton button = new JRadioButton(new AbstractAction(v.toString()) {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        JRadioButton rb = (JRadioButton)e.getSource();
                                        String fn = (String)rb.getClientProperty(PFX + ".NAME");
                                        Object fo = rb.getClientProperty(PFX + ".OBJECT");
                                        ButtonGroup bg = (ButtonGroup)rb.getClientProperty(PFX + ".GROUP");
                
                                        if(bg.getSelection() == rb.getModel()) {
                                            String v = (String)rb.getClientProperty(PFX + ".VALUE");
                                            try {
                                                Utils.parse(fo, fn, v);
                                            } catch(Exception ex) {
                                                ex.printStackTrace(System.out);
                                            }
                                        }
                                    }
                                });

                                button.putClientProperty(PFX + ".NAME", name);
                                button.putClientProperty(PFX + ".OBJECT", o);
                                button.putClientProperty(PFX + ".GROUP", group);
                                button.putClientProperty(PFX + ".VALUE", v.toString());
                                group.add(button);
                                if(v.toString().equals(item)) {
                                    selected = button;
                                }

                                flowPanel.add(button);
                                editorPanel.add(flowPanel);
                            }
                            if(selected != null) {
                                selected.setSelected(true);
                            }
                        } else {
                            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                            JComboBox<Object> combo = new JComboBox<>();

                            for(Object v : values) {
                                combo.addItem(v);
                            }
                            combo.putClientProperty(PFX + ".NAME", name);
                            combo.putClientProperty(PFX + ".OBJECT", o);
                            for(int i = 0; i != values.length; i++) {
                                String v = values[i].toString();

                                if(v.equals(item)) {
                                    combo.setSelectedIndex(i);
                                    break;
                                }
                            }

                            flowPanel.add(combo);
                            flowPanel.add(new JLabel(name, JLabel.LEFT));
                            editorPanel.add(flowPanel);

                            combo.addItemListener((e) -> {
                                JComboBox<Object> cb = (JComboBox<Object>)e.getSource();
                                String fname = (String)cb.getClientProperty(PFX + ".NAME");
                                Object fo = cb.getClientProperty(PFX + ".OBJECT");

                                try {
                                    Utils.parse(fo, fname, cb.getSelectedItem().toString());
                                } catch(Exception ex) {
                                    ex.printStackTrace(System.out);
                                }
                            });
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
    }


    private JFrame frame;
    private String[] topBar = new String[] {
        "Rot", "Zoom", "PanXZ", "PanY", "Sel", "MovXZ", "MovY", "RX", "RY", "RZ", "Scale", "Component"
    };
    private int mode = 0;
    private boolean lDown = false;
    private Hashtable<String, JToggleButton> toggleButtons = new Hashtable<>();
    private Hashtable<String, JButton> buttons = new Hashtable<>();
    private JTextArea consoleTextArea;
    private Game game;
    private Scene scene = null;
    private Node selected = null;
    private Node clipboard = null;
    private NodeComponentFactory[] componentFactories;
    private JTree tree;
    private DefaultTreeModel model;
    private JPopupMenu menu;
    private Vector3f origin = new Vector3f();
    private Vector3f direction = new Vector3f();
    private float[] time = new float[1];
    private Triangle triangle = new Triangle();
    private AABB bounds = new AABB();
    private Vector3f point = new Vector3f();
    private File loadSceneFile = null;
    private File loadMeshFile = null;
    private File loadKeyFrameMeshFile = null;
    private File playSceneFile = null;
    private boolean paste = false;
    private Icon deleteIcon;
    private Icon addIcon;
    private boolean toggleSync;
    private EditorPane editorPane;
    private final Matrix4f matrix = new Matrix4f();
    private boolean skipTreeSelectionEvent = false;

    public GameEditor(int w, int h, boolean resizable, boolean fixedFrameRate, Class<?> ... componentFactories) throws Exception {

        try {
            deleteIcon = new ImageIcon(load("/org/jge/resources/delete.png"));
            addIcon = new ImageIcon(load("/org/jge/resources/add.png"));
        } catch(Exception ex) {
        }

        frame = new JFrame("JGE-Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        for(String name : topBar) {
            toggleButtons.put(name, new JToggleButton(
                new AbstractAction(name) {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        for(int i = 0; i != topBar.length; i++) {
                            toggleButtons.get(topBar[i]).setSelected(e.getSource() == toggleButtons.get(topBar[i]));
                            if(toggleButtons.get(topBar[i]).isSelected()) {
                                mode = i;
                            }
                        }
                    };
                }
            ));
            topPanel.add(toggleButtons.get(name));
        }
        toggleButtons.get("Rot").setSelected(true);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttons.put("AddScene", new JButton(
            new AbstractAction("Scene") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    createScene();
                };
            }
        ));
        buttons.get("AddScene").setIcon(addIcon);
        bottomPanel.add(buttons.get("AddScene"));

        buttons.put("Load", new JButton(
            new AbstractAction("Load") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File file = Utils.selectFile(frame, IO.file("scenes"), ".xml");

                    if(file != null) {
                        loadSceneFile = file;
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Load"));

        buttons.put("Save", new JButton(
            new AbstractAction("Save") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        SceneSerializer.serialize(scene, scene.file);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Save"));

        buttons.put("Play", new JButton(
            new AbstractAction("Play") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    boolean inDesign = !scene.isInDesign();
                    
                    if(inDesign) {
                        loadSceneFile = scene.file;
                    } else {
                        playSceneFile = scene.file;
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Play"));

        buttons.put("Scene", new JButton(
            new AbstractAction("Scene") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    edit(scene);
                };
            }
        ));
        bottomPanel.add(buttons.get("Scene"));

        buttons.put("ZRot", new JButton(
            new AbstractAction("Z Rot") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotation.identity();
                };
            }
        ));
        bottomPanel.add(buttons.get("ZRot"));


        buttons.put("X45", new JButton(
            new AbstractAction("RX 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(0, (float)Math.toRadians(45));
                };
            }
        ));
        bottomPanel.add(buttons.get("X45"));

        buttons.put("Y45", new JButton(
            new AbstractAction("RY 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(1, (float)Math.toRadians(45));
                };
            }
        ));
        bottomPanel.add(buttons.get("Y45"));

        buttons.put("Z45", new JButton(
            new AbstractAction("RZ 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(2, (float)Math.toRadians(45));
                };
            }
        ));
        bottomPanel.add(buttons.get("Z45"));

        buttons.put("TargTo", new JButton(
            new AbstractAction("Targ To") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Vector3f o = scene.calcOffset();
                    scene.target.set(selected.absolutePosition);
                    scene.target.add(o, scene.eye);
                };
            }
        ));
        bottomPanel.add(buttons.get("TargTo"));

        buttons.put("ToTarg", new JButton(
            new AbstractAction("To Targ") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.position.set(scene.target);
                };
            }
        ));
        bottomPanel.add(buttons.get("ToTarg"));

        buttons.put("UScale", new JButton(
            new AbstractAction("U Scale") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.scale.set(1, 1, 1);
                };
            }
        ));
        bottomPanel.add(buttons.get("UScale"));

        buttons.put("Clear", new JButton(
            new AbstractAction("Clear") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    consoleTextArea.setText("");
                };
            }
        ));
        bottomPanel.add(buttons.get("Clear"));

        toggleButtons.put("Pause", new JToggleButton(
            new AbstractAction("Pause") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    TextAreaStream.consolePaused = !TextAreaStream.consolePaused;
                    toggleButtons.get("Pause").setSelected(TextAreaStream.consolePaused);
                };
            }
        ));
        bottomPanel.add(toggleButtons.get("Pause"));

        JToggleButton fixedFrameButton = new JToggleButton(
            new AbstractAction("Fixed Frame") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleSync = true;
                };
            }
        );
        bottomPanel.add(fixedFrameButton);
        fixedFrameButton.setSelected(fixedFrameRate);

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        System.setOut(new PrintStream(new TextAreaStream(consoleTextArea)));

        this.componentFactories = new NodeComponentFactory[componentFactories.length];
        for(int i = 0; i != componentFactories.length; i++) {
            Class<?> factory = componentFactories[i];

            System.out.println("found component factory: " + factory.getName());
            this.componentFactories[i] = new NodeComponentFactory(factory);
        }

        model = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.setCellRenderer(new TreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {
                String prefix = (leaf) ? "  " : ((expanded) ? "- " : "+ ");
                JLabel label = new JLabel(prefix + value);

                if(selected) {
                    label.setOpaque(true);
                    label.setBackground(Color.BLACK);
                } else {
                    label.setOpaque(false);
                }
                return label;
            }
            
        });

        tree.addTreeSelectionListener((e) -> {
            TreePath path = tree.getSelectionPath();

            if(path != null) {
                try {
                    if(!skipTreeSelectionEvent) {
                        Object c = path.getLastPathComponent();

                        if(c instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode tree = (DefaultMutableTreeNode)c;
                            
                            selected = (Node)tree.getUserObject();
                        }
                    }
                } finally {
                    skipTreeSelectionEvent = false;
                }
            }
            enableUI();
            edit(selected);
        });

        menu = new JPopupMenu();
        menu.add(new JMenuItem(new AbstractAction("Add Mesh") {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = Utils.selectFile(frame, IO.file("assets"), ".obj");

                if(file != null) {
                    loadMeshFile = file;
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Add Key Frame Mesh") {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = Utils.selectFile(frame, IO.file("assets"), ".kfm");

                if(file != null) {
                    loadKeyFrameMeshFile = file;
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Add Node") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node parent = selected;
                Node node = new Node();

                if(parent == null) {
                    parent = scene.root;
                }
                parent.addChild(node);

                populateTree();
                select(node);
                enableUI();
            }
        }));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    try {
                        clipboard = new Node(scene, selected);
                        selected.detach();
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    try {
                        clipboard = new Node(scene, selected);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clipboard != null) {
                    paste = true;
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    selected.detach();
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Clear Selection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == 3) {
                    boolean enabled = scene != null;

                    if(enabled) {
                        enabled = scene.isInDesign();
                    }
                    if(enabled) {
                        menu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        });


        JScrollPane treePane = new JScrollPane(tree);
        JScrollPane consolePane = new JScrollPane(consoleTextArea);
        JPanel consolePanel = new JPanel(new BorderLayout());
        
        editorPane = new EditorPane(300);

        treePane.setPreferredSize(new Dimension(200, 100));;
        consolePane.setPreferredSize(new Dimension(100, 150));

        game = new Game(w, h, fixedFrameRate, this);
        frame.add(game.getCanvas(), BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(treePane, BorderLayout.WEST);
        frame.add(editorPane.getScrollPane(), BorderLayout.EAST);

        consolePanel.add(consolePane, BorderLayout.CENTER);
        consolePanel.add(bottomPanel, BorderLayout.NORTH);

        frame.add(consolePanel, BorderLayout.SOUTH);

        enableUI();

        frame.pack();
        frame.setVisible(true);

        game.start();
    }

    private BufferedImage load(String name) throws IOException {
        InputStream input = null;

        try {
            return ImageIO.read(input = GameEditor.class.getResourceAsStream(name));
        } finally {
            if(input != null) {
                input.close();
            }
        }
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void render() throws Exception {
        if(scene != null) {
            try {
                if(scene.refreshSceneTree) {
                    TreePath path = tree.getSelectionPath();

                    skipTreeSelectionEvent = true;

                    populateTree();

                    skipTreeSelectionEvent = true;

                    tree.setSelectionPath(path);
                }
            } finally {
                scene.refreshSceneTree = false;
            }
        }
        if(loadSceneFile != null) {
            try {
                game.getAssets().clear();
                scene = SceneSerializer.deserialize(true, loadSceneFile);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                loadSceneFile = null;
            }
            populateTree();
            select(null);
            enableUI();
        } else if(playSceneFile != null) {
            try {
                scene = null;
                game.getAssets().clear();
                scene = SceneSerializer.deserialize(false, playSceneFile);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                playSceneFile = null;
            }
            populateTree();
            select(null);
            enableUI();
        } else if(loadMeshFile != null) {
            Node node = null;
            try {
                Mesh mesh = game.getAssets().load(loadMeshFile);

                node = new Node();
                node.renderable = mesh;
                node.name = IO.getFilenameWithoutExtension(loadMeshFile);

                Node parent = scene.root;

                if(selected != null) {
                    parent = selected;
                }
                parent.addChild(node);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                loadMeshFile = null;
            }
            populateTree();
            select(node);
            enableUI();
        } else if(loadKeyFrameMeshFile != null) {
            Node node = null;
            try {
                KeyFrameMesh mesh = game.getAssets().load(loadKeyFrameMeshFile);

                mesh = (KeyFrameMesh)mesh.newInstance();
                node = new Node();
                node.renderable = mesh;
                node.name = IO.getFilenameWithoutExtension(loadKeyFrameMeshFile);

                Node parent = scene.root;

                if(selected != null) {
                    parent = selected;
                }
                parent.addChild(node);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                loadKeyFrameMeshFile = null;
            }
            populateTree();
            select(node);
            enableUI();
        } else if(toggleSync) {
            try {
                GL2 gl = Game.getGL();
                
                if(gl.getSwapInterval() == 0) {
                    gl.setSwapInterval(1);
                } else {
                    gl.setSwapInterval(0);
                }
            } finally {
                toggleSync = false;
            }
        }
        
        if(scene == null) {
            GFX.clear(0.2f, 0.2f, 0.2f, 1);
        } else {
            try {
                if(paste) {
                    Node parent = selected;
                    Node node = null;

                    if(parent == null) {
                        parent = scene.root;
                    }
                    try {
                        node = new Node(scene, clipboard);
                        parent.addChild(node);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    } finally {
                        paste = false;
                    }
                    populateTree();
                    select(node);
                    enableUI();
                }
                game.getSceneRenderer().render(scene);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }

            if(scene.isInDesign()) {
                handleInput();
            } else {
                scene = Scene.next(scene);
                if(scene == null) {
                    enableUI();
                }
            }
        }
    }

    private void handleInput() {
        if(game.buttonDown(0)) {
            if(mode == ROT) {
                scene.rotate(game.dX() * 0.02f, game.dY() * 0.02f);
            } else if(mode == ZOOM) {
                scene.zoom(game.dY());
            }  else if(mode == PANXZ) {
                scene.move(scene.target, game.dX(), game.dY());
            } else if(mode == PANY) {
                scene.move(scene.target, game.dY());
            } else if(mode == SEL) {
                if(!lDown) {
                    int w = game.w();
                    int h = game.h();
                    int x = game.mouseX();
                    int y = h - game.mouseY() - 1;
                    
                    GFX.unproject(x, y, 0, 0, 0, w, h, scene.projection, scene.view, origin);
                    GFX.unproject(x, y, 1, 0, 0, w, h, scene.projection, scene.view, direction);

                    direction.sub(origin).normalize();
                    time[0] = Float.MAX_VALUE;

                    selected = null;
                    try {
                        scene.root.traverse((n) -> {
                            bounds.clear();
                            bounds.add(origin);
                            bounds.add(point.set(direction).mul(time[0]).add(origin));
                            if(n.bounds.touches(bounds)) {
                                for(int i = 0; i != n.getTriangleCount(); i++) {
                                    n.getTriangle(scene, i, triangle);
                                    if(triangle.n.dot(direction) < 0) {
                                        if(triangle.intersects(origin, direction, 0, time)) {
                                            selected = n;
                                        }
                                    }
                                }
                            }
                            if(n.isLight) {
                                bounds.min.set(n.absolutePosition).sub(8, 8, 8);
                                bounds.max.set(n.absolutePosition).add(8, 8, 8);
                                if(bounds.isects(origin, direction, time)) {
                                    selected = n;
                                }
                            }
                            return true;
                        });
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    select(selected);
                    enableUI();
                }
            } else if(selected != null) {
                matrix.set(selected.getParent().model).invert();
                if(mode == MOVXZ) {
                    scene.move(selected.position, -game.dX(), -game.dY(), matrix);
                } else if(mode == MOVY) {
                    scene.move(selected.position, -game.dY(), matrix);
                } else if(mode == RX) {
                    selected.rotate(0, game.dX() * 0.02f);
                } else if(mode == RY) {
                    selected.rotate(1, game.dX() * 0.02f);
                } else if(mode == RZ) {
                    selected.rotate(2, game.dX() * 0.02f);
                } else if(mode == SCALE) {
                    if(game.dX() < 0) {
                        selected.scale.mul(0.9f);
                    } else if(game.dX() > 0) {
                        selected.scale.mul(1.1f);
                    }
                } 
            }
            lDown = true;
        } else {
            if(!lDown && selected != null) {
                if(mode == MOVXZ || mode == MOVY) {
                    Vector3f p = selected.position;

                    p.x = Math.round(p.x / scene.snap) * scene.snap;
                    p.y = Math.round(p.y / scene.snap) * scene.snap;
                    p.z = Math.round(p.z / scene.snap) * scene.snap;
                }
            }
            lDown = false;
        }
        if(mode == COMPONENT && selected != null) {
            for(int i = 0; i != selected.getComponentCount(); i++) {
                try {
                    selected.getComponent(i).handleInput();
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    private void enableUI() {
        boolean enabled = scene != null;

        if(enabled) {
            enabled = scene.isInDesign();
        }

        Enumeration<String> e = toggleButtons.keys();

        while(e.hasMoreElements()) {
            String key = e.nextElement();

            if(!key.equals("Pause")) {
                toggleButtons.get(key).setEnabled(enabled);
            }
        }

        e = buttons.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();

            if(!key.equals("Play")) {
                if(!key.equals("Clear")) {
                    if(key.equals("Load") || key.equals("AddScene")) {
                        buttons.get(key).setEnabled(enabled || scene == null);
                    } else if(key.equals("SnapShot")) {
                        buttons.get(key).setEnabled(scene != null);
                    } else if(
                        key.equals("ToTarg") ||
                        key.equals("TargTo") ||
                        key.equals("ZRot") ||
                        key.equals("X45") ||
                        key.equals("Y45") ||
                        key.equals("Z45") || 
                        key.equals("UScale")
                    ) {
                        buttons.get(key).setEnabled(enabled && selected != null);
                    } else {
                        buttons.get(key).setEnabled(enabled);
                    }
                }
            } else {
                buttons.get(key).setEnabled(scene != null);
                buttons.get(key).setText((enabled) ? "Play" : "Stop");
            }
        }
    }

    private void populateTree() {
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode();

        if(scene != null) {
            if(scene.isInDesign()) {
                for(Node node : scene.root) {
                    populateTree(tree, node);
                }
            }
        }
        model.setRoot(tree);
    }

    private void populateTree(DefaultMutableTreeNode parent, Node node) {
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode(node);

        parent.add(tree);
        for(Node child : node) {
            populateTree(tree, child);
        }
    }

    private void select(Node node) {
        selected = null;
        if(node != null) {
            select(node, (DefaultMutableTreeNode)model.getRoot());
        } else {
            tree.clearSelection();
            edit(null);
        }
    }

    private void select(Node node, DefaultMutableTreeNode treeNode) {
        if(treeNode.getUserObject() == node) {
            tree.setSelectionPath(new TreePath(treeNode.getPath()));
        }
        for(int i = 0; i != treeNode.getChildCount(); i++) {
            select(node, (DefaultMutableTreeNode)treeNode.getChildAt(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void edit(Object o) {

        editorPane.begin();

        if(o == null) {
            editorPane.end();
            return;
        }

        JPanel editorPanel = editorPane.getEditorPanel();

        if(o != selected) {
            selected = null;
            tree.clearSelection();
        } else {

        }

        editorPane.addFields(o, tree, model, Node.class);

        if(o == selected) {
            for(int i = 0; i != selected.getComponentCount(); i++) {
                NodeComponent component = selected.getComponent(i);
                JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                JButton button = new JButton(new AbstractAction(component.getClass().getSimpleName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JButton b = (JButton)e.getSource();
                        NodeComponent c = (NodeComponent)b.getClientProperty(EditorPane.PFX + ".COMPONENT");

                        c.remove();

                        edit(selected);
                    }
                });
                String name = component.getClass().getSimpleName();
                JLabel label = new JLabel(name);
                JPanel flowPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

                button.setIcon(deleteIcon);

                label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, 14));
                label.setForeground(Color.WHITE);
                button.putClientProperty(EditorPane.PFX + ".COMPONENT", component);
                flowPanel2.add(label);
                editorPanel.add(flowPanel2);
                editorPane.addFields(component, tree, model, Node.class);
                flowPanel.add(button);
                editorPanel.add(flowPanel);
            }

            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            JButton button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton b = (JButton)e.getSource();
                    JComboBox<String> combo = (JComboBox<String>)b.getClientProperty(EditorPane.PFX + ".COMBO");
                    
                    try {
                        NodeComponent component = (NodeComponent)((NodeComponentFactory)combo.getSelectedItem()).cls.getConstructors()[0].newInstance();

                        selected.addComponent(scene, component);

                        edit(selected);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            });
            JComboBox<NodeComponentFactory> combo = new JComboBox<>();

            button.setIcon(addIcon);
            for(NodeComponentFactory factory : componentFactories) {
                combo.addItem(factory);
            }
            combo.setSelectedIndex(0);
            flowPanel.add(combo);
            flowPanel.add(button);
            editorPanel.add(flowPanel, o);

            button.putClientProperty(EditorPane.PFX + ".COMBO", combo);
        }
        editorPane.end();
    }

   

    private void createScene() {
        Object r = JOptionPane.showInputDialog(frame, "Scene Name", "");

        if(r != null) {
            String name = ((String)r).trim();

            if(name.length() == 0) {
                System.out.println("name is blank");
                return;
            }
            
            for(int i = 0; i != name.length(); i++) {
                char c = name.charAt(i);

                if(!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
                    System.out.println("name can only contain letter, digit or '_' characters");
                    return;
                }
            }

            File file = new File(new File("scenes"), name + ".xml");

            if(file.exists()) {
                System.out.println("scene already exists");
                return;
            }

            try {
                IO.writeAllBytes("<scene/>".getBytes(), file);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}