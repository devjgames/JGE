package org.game;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

public class MeshLoader {
    
    public static final UUID NAME = UUID.randomUUID();
    public static final UUID AMBIENT_COLOR = UUID.randomUUID();
    public static final UUID DIFFUSE_COLOR = UUID.randomUUID();
    public static final UUID TEXTURE = UUID.randomUUID();

    public static class OBJVertex {
        public final Vec3 position = new Vec3();
        public final Vec2 textureCoordinate = new Vec2();
        public final Vec3 normal = new Vec3();
    }

    public static class OBJMeshPart {
        public String name = "";
        public Texture texture = null;
        public final AABB bounds = new AABB();
        public final Vector<OBJVertex> vertices = new Vector<>();
        public final Vector<int[]> faces = new Vector<>();
        public int indexCount = 0;

        public void calcBounds() {
            bounds.clear();
            for(OBJVertex vertex : vertices) {
                bounds.union(vertex.position);
            }
        }
    }

    public static class OBJMesh {
        public final Vec3 position = new Vec3();
        public final AABB bounds = new AABB();
        public final Vector<OBJMeshPart> parts = new Vector<>();

        public void calcBounds() {
            bounds.clear();
            for(OBJMeshPart part : parts) {
                part.calcBounds();
                bounds.union(part.bounds);
            }
        }
    }

    static Mesh load(File file) throws Exception {
        String[] lines = new String(IOHelper.readAllBytes(file)).split("\\n+");
        OBJMesh objMesh = new OBJMesh();
        OBJMeshPart objMeshPart = null;
        Vector<Vec3> vList = new Vector<>(10000);
        Vector<Vec2> tList = new Vector<>(10000);
        Vector<Vec3> nList = new Vector<>(10000);
        String material = "";
        Hashtable<String, String> textureNames = new Hashtable<>();
        Hashtable<String, Texture> textures = new Hashtable<>(); 

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                String[] mLines = new String(
                    IOHelper.readAllBytes(
                        new File(file.getParent(), tLine.substring(6).trim())
                    )
                ).split("\\n+");
                String m = "";

                for(String mLine : mLines) {
                    String tmLine = mLine.trim();
                    
                    if(tmLine.startsWith("newmtl ")) {
                        m = tmLine.substring(6).trim();
                        textureNames.put(m, "");
                    } else if(tmLine.startsWith("map_Kd ")) {
                        String tn = tmLine.substring(6).trim();
                        int i = tn.lastIndexOf(File.separatorChar);

                        if(i != -1) {
                            tn = tn.substring(i + 1);
                        }
                        textureNames.put(m, tn);
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                material = tLine.substring(6).trim();
                objMeshPart = new OBJMeshPart();
                objMeshPart.name = material;
                loadTexture(file, textureNames.get(material), textures, objMeshPart);
                objMesh.parts.add(objMeshPart);
            } else if(tLine.startsWith("v ")) {
                vList.add(new Vec3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tLine.startsWith("vt ")) {
                tList.add(new Vec2(Float.parseFloat(tokens[1]), 1 - Float.parseFloat(tokens[2])));
            } else if(tLine.startsWith("vn ")) {
                nList.add(new Vec3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tLine.startsWith("f ")) {
                if(objMeshPart == null) {
                    objMeshPart = new OBJMeshPart();
                    objMeshPart.name = material;
                    loadTexture(file, textureNames.get(material), textures, objMeshPart);
                    objMesh.parts.add(objMeshPart);
                }

                int[] face = new int[tokens.length - 1];
                int i = 0;
                int n = objMeshPart.vertices.size();

                for(int j = 1; j != tokens.length; j++) {
                    String[] iTokens = tokens[j].split("/");
                    int iV = Integer.parseInt(iTokens[0]) - 1;
                    int iT = Integer.parseInt(iTokens[1]) - 1;
                    int iN = Integer.parseInt(iTokens[2]) - 1;
                    OBJVertex vertex = new OBJVertex();

                    vertex.position.set(vList.get(iV));
                    vertex.textureCoordinate.set(tList.get(iT));
                    vertex.normal.set(nList.get(iN));

                    objMeshPart.vertices.add(vertex);
                    face[i] = n + i;
                    i++;
                }
                objMeshPart.indexCount += (face.length - 2) * 3;
                objMeshPart.faces.add(face);
            }
        }

        objMesh.calcBounds();

        Mesh mesh = new Mesh();

        mesh.bounds.set(objMesh.bounds);
        mesh.put(AMBIENT_COLOR, new Vec4(0.2f, 0.2f, 0.2f, 1));
        mesh.put(DIFFUSE_COLOR, new Vec4(1, 1, 1, 1));

        for(OBJMeshPart objPart : objMesh.parts) {
            if(objPart.indexCount != 0) {
                MeshPart part = new MeshPart(0, 0, mesh);

                if(objPart.texture != null) {   
                    part.put(TEXTURE, objPart.texture);
                }
                part.put(NAME, objPart.name);
                part.bounds.set(objPart.bounds);
                part.getIBounds().set(part.bounds);

                part.vertexBuffer.begin(objPart.vertices.size() * BasicRenderer.VERTEX_STRIDE);
                for(OBJVertex vertex : objPart.vertices) {
                    part.vertexBuffer.push(vertex.position);
                    part.vertexBuffer.push(vertex.textureCoordinate);
                    part.vertexBuffer.push(vertex.normal);
                    part.vertexBuffer.push(1, 1, 1, 1);
                }
                part.vertexBuffer.end(BufferUsage.STATIC);

                part.vertexElementBuffer.begin(objPart.indexCount);
                for(int[] face : objPart.faces) {
                    part.vertexElementBuffer.push(face);
                }
                part.vertexElementBuffer.end(BufferUsage.STATIC);

                mesh.addPart(part);
            }
        }
        return mesh;
    }

    private static void loadTexture(File objFile, String name, Hashtable<String, Texture> textures, OBJMeshPart part) throws Exception {
        if(name != null) {
            if(!name.isEmpty()) {
                Game game = Game.getInstance();
                File texFile = new File(objFile.getParentFile(), name);

                if(texFile.exists()) {
                    if(!textures.containsKey(name)) {
                        int i = game.getResources().getRoot().getPath().length();

                        texFile = new File(texFile.getPath().substring(i + 1));
                        textures.put(name, game.getResources().load(texFile));
                    }
                    if(textures.containsKey(name)) {
                        part.texture = textures.get(name);
                    }
                }
            }
        }
    }
}
