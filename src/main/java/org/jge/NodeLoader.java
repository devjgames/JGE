package org.jge;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class NodeLoader implements AssetLoader {
    

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>();
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Node root = new Node();
        Node objNode = null;
        Node matNode = null;
        Hashtable<String, String> materials = new Hashtable<>();
        String material = "";

        for (String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");
            if(tLine.startsWith("mtllib ")) {
                loadMaterials(new File(file.getParent(), tLine.substring(6).trim()), materials);
            } else if(tLine.startsWith("usemtl ")) {
                material = materials.get(tLine.substring(6).trim());
                if(objNode == null) {
                    objNode = new Node();
                    root.addChild(objNode);
                }
                matNode = new Node();
            
                File texFile = IO.file(material);

                matNode.texture = assets.load(texFile);
                matNode.name = IO.getFilenameWithoutExtension(texFile);
                objNode.addChild(matNode);
            } else if(tLine.startsWith("o ")) {
                objNode = new Node();
                objNode.name = tLine.substring(1).trim();
                root.addChild(objNode);
            } else if (tLine.startsWith("v ")) {
                vList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("vt ")) {
                tList.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));;
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if (tLine.startsWith("vn ")) {
                nList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("f ")) {
                if(objNode == null) {
                    objNode = new Node();
                    root.addChild(objNode);
                }
                if(matNode == null) {
                    matNode = new Node();
                    objNode.addChild(matNode);
                }

                if(matNode.renderable == null) {
                    matNode.renderable = new Mesh();
                }

                Mesh mesh = (Mesh)matNode.renderable;
                int bV = mesh.vertices.size();
                int[] polygon = new int[tokens.length - 1];
                
                for (int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("[/]+");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    VertexPT2N vertex = new VertexPT2N();

                    vertex.position.set(vList.get(vI));
                    vertex.textureCoordinate.set(tList.get(tI));
                    vertex.normal.set(nList.get(nI));
                    mesh.vertices.add(vertex);

                    polygon[i - 1] = bV + (i - 1);
                }
                mesh.addPolygon(polygon);
            }
        }

        for(Node obj : root) {
            for(Node mat : obj) {
                if(mat.renderable != null) {
                    Mesh mesh = (Mesh)mat.renderable;

                    mesh.calcBounds();;
                }
            }
        }
        return root;
    }

    private void loadMaterials(File file, Hashtable<String, String> materials) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        String name = null;

        for(String line : lines) {
            String tLine = line.trim();
            if(tLine.startsWith("newmtl ")) {
                name = tLine.substring(6).trim();
            } else if(tLine.startsWith("map_Kd ")) {
                materials.put(name, new File(file.getParent(), tLine.substring(6).trim()).getPath());
            }
        }
    }
}
