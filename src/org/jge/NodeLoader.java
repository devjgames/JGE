package org.jge;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
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
        Hashtable<String, Mesh> keyedMeshes = new Hashtable<>();
        Hashtable<String, String> materials = new Hashtable<>();
        HashSet<String> textures = new HashSet<>();
        String material = "";

        for (String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");
            if(tLine.startsWith("mtllib ")) {
                loadMaterials(new File(file.getParent(), tLine.substring(6).trim()), materials);
            } else if(tLine.startsWith("usemtl ")) {
                material = materials.get(tLine.substring(6).trim());
                if(!keyedMeshes.containsKey(material)) {
                    textures.add(material);
                    keyedMeshes.put(material, new Mesh(null));
                }
            } else if (tLine.startsWith("v ")) {
                vList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("vt ")) {
                tList.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));;
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if (tLine.startsWith("vn ")) {
                nList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("f ")) {
                if(!keyedMeshes.containsKey(material)) {
                    keyedMeshes.put(material, new Mesh(null));
                }
                Mesh mesh = keyedMeshes.get(material);
                int bV = mesh.vertices.size();
                int tris = tokens.length - 3;
                for (int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("[/]+");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    VertexPTN vertex = new VertexPTN();

                    vertex.position.set(vList.get(vI));
                    vertex.textureCoordinate.set(tList.get(tI));
                    vertex.normal.set(nList.get(nI));
                    mesh.vertices.add(vertex);
                }
                for (int i = 0; i != tris; i++) {
                    mesh.indices.add(bV);
                    mesh.indices.add(bV + i + 1);
                    mesh.indices.add(bV + i + 2);
                }
            }
        }
        Enumeration<String> keys = keyedMeshes.keys();
        Vector<String> sortedKeys = new Vector<>();
        while(keys.hasMoreElements()) {
            sortedKeys.add(keys.nextElement());
        }
        sortedKeys.sort((a, b) -> a.compareTo(b));
        Node root = new Node();

        root.name = IO.getFilenameWithoutExtension(file);
        for(String key : sortedKeys) {
            Mesh mesh = keyedMeshes.get(key);

            if(mesh.indices.size() != 0) {
                Node node = new Node();

                node.name = key;
                node.renderable = mesh;
                if(textures.contains(key)) {
                    node.texture = assets.load(IO.file(key));
                    node.name = IO.getFilenameWithoutExtension(node.texture.file);
                }
                mesh.calcBounds();

                root.addChild(node);
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
