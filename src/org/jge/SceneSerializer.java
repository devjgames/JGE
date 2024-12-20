package org.jge;

import java.io.File;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SceneSerializer {
    

    public static Scene deserialize(boolean inDesign, File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document =  builder.parse(file);

        return (Scene)load(new Scene(file, inDesign), document.getDocumentElement());
    }

    private static Object load(Scene scene, Element element) throws Exception {
        NodeList nodes = element.getChildNodes();
        Object r = null;

        if(element.getTagName().equals("scene")) {
            load((Object)scene, element);

            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node xmlNode = nodes.item(i);

                if(xmlNode instanceof Element) {
                    Element element2 = (Element)xmlNode;

                    scene.root.addChild((Node)load(scene, element2));
                }
            }
            r = scene;
        } else {
            Node node = new Node();

            load(node, element);

            if(element.hasAttribute("renderable")) {
                try {
                    Renderable renderable = Game.getInstance().getAssets().load(IO.file(element.getAttribute("renderable")));

                    node.renderable = renderable.newInstance();
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                } 
            }
            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node xmlNode = nodes.item(i);

                if(xmlNode instanceof Element) {
                    Element element2 = (Element)xmlNode;

                    if(element2.getTagName().equals("node")) {
                        node.addChild((Node)load(scene, element2));
                    } else {
                        try {
                            NodeComponent component = (NodeComponent)Class.forName(element2.getTagName()).getConstructors()[0].newInstance();

                            load(component, element2);
                            node.addComponent(scene, component);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                }
            }
            r = node;
        }
        return r;
    }

    private static void load(Object o, Element element) throws Exception {
        Field[] fields = o.getClass().getFields();

        for(Field field : fields) {
            String name = field.getName();

            if(element.hasAttribute(name)) {
                try {
                    Utils.parse(o, name, element.getAttribute(name));
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }
    
    public static void serialize(Scene scene, File file) throws Exception {
        StringBuilder b = new StringBuilder(10000);

        b.append("<scene");
        append(scene, false, b);
        for(Node node : scene.root) {
            append(node, "\t", b);
        }
        b.append("</scene>\n");

        IO.writeAllBytes(b.toString().getBytes(), file);
    }

    private static void append(Object o, boolean empty, StringBuilder b) throws Exception {
        Class<? extends Object> cls = o.getClass();
        Field[] fields = cls.getFields();

        for(Field field : fields) {
            String s = Utils.toString(o, field.getName());

            if(s != null) {
                b.append(" " + field.getName() + "=\"" + fix(s) + "\"");
            }
        }
        if(empty) {
            b.append("/>\n");
        } else {
            b.append(">\n");
        }
    }

    private static String fix(String value) {
        value = value.replace("&", "&amp;");
        value = value.replace("\"", "&quot;");
        value = value.replace("'", "&apos;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\n", "&#10;");
        value = value.replace("\t", "&#09;");
        return value;
    }

    private static void append(Node node, String indent, StringBuilder b) throws Exception {
        boolean empty = node.getChildCount() == 0 && node.getComponentCount() == 0;

        b.append(indent + "<node");
        if(node.renderable != null) {
            File file = node.renderable.getFile();

            if(file != null) {
                b.append(" renderable=\"" + file.getPath() + "\"");
            }
        }
        append(node, empty, b);
        if(!empty) {
            for(int i = 0; i != node.getComponentCount(); i++) {
                NodeComponent component = node.getComponent(i);

                b.append(indent + "\t<" + component.getClass().getName());
                append(component, true, b);
            }
            for(Node child : node) {
                append(child, indent + "\t", b);
            }
            b.append(indent + "</node>\n");
        }
    }
}
