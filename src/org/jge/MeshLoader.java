package org.jge;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshLoader implements AssetLoader {
    

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>();
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Mesh mesh = new Mesh(file);
        Hashtable<String, Mesh.MeshPart> keyedMeshParts = new Hashtable<>();
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
                if(!keyedMeshParts.containsKey(material)) {
                    textures.add(material);
                    keyedMeshParts.put(material, new Mesh.MeshPart());
                }
            } else if (tLine.startsWith("v ")) {
                vList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("vt ")) {
                tList.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));;
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if (tLine.startsWith("vn ")) {
                nList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tLine.startsWith("f ")) {
                if(!keyedMeshParts.containsKey(material)) {
                    keyedMeshParts.put(material, new Mesh.MeshPart());
                }
                Mesh.MeshPart meshPart = keyedMeshParts.get(material);
                int bV = meshPart.vertices.size();
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
                    meshPart.vertices.add(vertex);

                    polygon[i - 1] = bV + (i - 1);
                }
                meshPart.addPolygon(polygon);
            }
        }
        Enumeration<String> keys = keyedMeshParts.keys();
        Vector<String> sortedKeys = new Vector<>();
        while(keys.hasMoreElements()) {
            sortedKeys.add(keys.nextElement());
        }
        sortedKeys.sort((a, b) -> a.compareTo(b));

        for(String key : sortedKeys) {
            Mesh.MeshPart meshPart = keyedMeshParts.get(key);

            if(meshPart.indices.size() != 0) {
                if(textures.contains(key)) {
                    File tfile;
                    meshPart.texture = assets.load(tfile = IO.file(key));

                    tfile = IO.file(tfile.getParentFile(), IO.getFilenameWithoutExtension(tfile) + "_DECAL.png");
                    if(tfile.exists()) {
                        meshPart.decal = assets.load(tfile);
                    }
                }
                meshPart.calcBounds();
                mesh.parts.add(meshPart);
            }
        }
        mesh.calcBounds();

        return mesh;
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
