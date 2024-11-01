package org.jge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class EditorPane {

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

    public JPanel getEditorContainerPanel() {
        return editorContainerPanel;
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
