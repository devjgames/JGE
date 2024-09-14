package org.jge;

import java.io.File;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Serializer {
    

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

                    MD2Mesh mesh = null;

                    if(node.renderable instanceof MD2Mesh) {
                        mesh = (MD2Mesh)node.renderable;
                    }

                    if(mesh != null && element.hasAttribute("sequence")) {
                        String[] tokens = element.getAttribute("sequence").split("\\s+");

                        mesh.setSequence(
                            Integer.parseInt(tokens[0]),
                            Integer.parseInt(tokens[1]),
                            Integer.parseInt(tokens[2]),
                            Boolean.parseBoolean(tokens[3])
                        );
                    }
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            if(element.hasAttribute("texture")) {
                try {
                    node.texture = Game.getInstance().getAssets().load(IO.file(element.getAttribute("texture")));
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            if(element.hasAttribute("vertices") && element.hasAttribute("indices")) {
                String[] svertices = element.getAttribute("vertices").split("\\s+");
                String[] sindices = element.getAttribute("indices").split("\\s");
                Mesh mesh = new Mesh(null);

                for(int i = 0; i != svertices.length; ) {
                    float x = Float.parseFloat(svertices[i++]);
                    float y = Float.parseFloat(svertices[i++]);
                    float z = Float.parseFloat(svertices[i++]);
                    float s = Float.parseFloat(svertices[i++]);
                    float t = Float.parseFloat(svertices[i++]);
                    float nx = Float.parseFloat(svertices[i++]);
                    float ny = Float.parseFloat(svertices[i++]);
                    float nz = Float.parseFloat(svertices[i++]);
                    VertexPTN vertex = new VertexPTN();

                    vertex.position.set(x, y, z);
                    vertex.textureCoordinate.set(s, t);
                    vertex.normal.set(nx, ny, nz);

                    mesh.vertices.add(vertex);
                }
                mesh.calcBounds();

                for(int i = 0; i != sindices.length; i++) {
                    mesh.indices.add(Integer.parseInt(sindices[i]));
                }
                node.renderable = mesh;
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
        Mesh mesh = null;

        if(node.renderable instanceof Mesh) {
            mesh = (Mesh)node.renderable;
        }

        b.append(indent + "<node");
        if(mesh != null) {
            if(mesh.indices.size() != 0) {
                b.append(" vertices=\"");
                for(int i = 0; i != mesh.vertices.size(); i++) {
                    VertexPTN v = mesh.vertices.get(i);

                    if(i == 0) {
                        b.append(Utils.toString(v, "position"));
                    } else {
                        b.append(" " + Utils.toString(v, "position"));
                    }
                    b.append(" " + Utils.toString(v, "textureCoordinate"));
                    b.append(" " + Utils.toString(v, "normal"));
                }
                b.append("\" indices=\"");
                for(int i = 0; i != mesh.indices.size(); i++) {
                    if(i == 0) {
                        b.append(mesh.indices.get(i));
                    } else {
                        b.append(" " + mesh.indices.get(i));
                    }
                }
                b.append("\"");
            }
        } else if(node.renderable != null) {
            File file = node.renderable.getFile();

            if(file != null) {
                MD2Mesh md2Mesh = null;

                if(node.renderable instanceof MD2Mesh) {
                    md2Mesh = (MD2Mesh)node.renderable;
                }

                b.append(" renderable=\"" + file.getPath() + "\"");
                if(md2Mesh != null) {
                    b.append(
                        " sequence=\"" + 
                        md2Mesh.getStart() + " " + md2Mesh.getEnd() + " " + md2Mesh.getSpeed() + " " + md2Mesh.isLooping() + "\""
                        );
                }
            }
        }
        if(node.texture != null) {
            b.append(" texture=\"" + node.texture.file + "\"");
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
