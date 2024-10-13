package org.jge.demo;

import java.util.Vector;

import org.jge.Button;
import org.jge.Game;
import org.jge.IO;
import org.jge.Mesh;
import org.jge.MultiLine;
import org.jge.NodeComponent;
import org.jge.Mesh.MeshPart;

public class Map extends NodeComponent {
    
    @MultiLine
    public String map = "";
    public Button build = new Button() {
        public void onClick() {
            build();
        };
    };

    @Override
    public void init() throws Exception {
        build();
    }

    private void build() {
        try {
            Mesh mesh = new Mesh(null);
            MeshPart bricksMeshPart = new MeshPart();
            MeshPart stoneMeshPart = new MeshPart();

            mesh.parts.add(bricksMeshPart);
            mesh.parts.add(stoneMeshPart);

            bricksMeshPart.texture = Game.getInstance().getAssets().load(IO.file("assets/maps/bricks.png"));
            stoneMeshPart.texture = Game.getInstance().getAssets().load(IO.file("assets/maps/stone.png"));

            String[] lines = map.split("\\n+");
            int rows = 0;
            int cols = 0;
            Vector<String> mapLines = new Vector<>();

            for(String line : lines) {
                String tLine = line.trim();

                if(tLine.startsWith("*")) {
                    mapLines.add(tLine);
                    rows++;
                    if(cols == 0) {
                        cols = tLine.length();
                    } else if(cols != tLine.length()) {
                        throw new Exception("invalid row - '" + line + "'");
                    }
                } else if(tLine.length() > 0) {
                    throw new Exception("invalid row - '" + line + "'");
                }
            }

            boolean hasHalls = false;

            for(int row = 0; row != rows; row++) {
                String tLine = mapLines.get(row);

                for(int col = 0; col != cols; col++) {
                    char c = tLine.charAt(col);

                    if((row == 0 && c != '*') || (row == rows - 1 && c != '*') || (col == 0 && c != '*') || (col == cols - 1 && c != '*')) {
                        throw new Exception("map is not '*' bounded");
                    } else if (c != '*') {
                        hasHalls = true;
                    }
                }
            }

            if(!hasHalls) {
                throw new Exception("map does not have any halls");
            }

            final int dim = 128;

            for(int row = 1; row != rows - 1; row++) {
                String tLine = mapLines.get(row);

                for(int col = 1; col != cols - 1; col++) {
                    char c = tLine.charAt(col);

                    if(c != '*') {
                        int x1 = col * dim;
                        int y1 = 0;
                        int z1 = row * dim;
                        int x2 = x1 + dim;
                        int y2 = y1 + dim;
                        int z2 = z1 + dim;
                        int vc = stoneMeshPart.vertices.size();

                        stoneMeshPart.addVertex(x1, y1, z1, 0, 0, 0, 1, 0);
                        stoneMeshPart.addVertex(x1, y1, z2, 0, 2, 0, 1, 0);
                        stoneMeshPart.addVertex(x2, y1, z2, 2, 2, 0, 1, 0);
                        stoneMeshPart.addVertex(x2, y1, z1, 2, 0, 0, 1, 0);
                        stoneMeshPart.addPolygon(vc, vc + 1, vc + 2, vc + 3);

                        vc = stoneMeshPart.vertices.size();
                        stoneMeshPart.addVertex(x1, y2, z1, 0, 0, 0, -1, 0);
                        stoneMeshPart.addVertex(x1, y2, z2, 0, 2, 0, -1, 0);
                        stoneMeshPart.addVertex(x2, y2, z2, 2, 2, 0, -1, 0);
                        stoneMeshPart.addVertex(x2, y2, z1, 2, 0, 0, -1, 0);
                        stoneMeshPart.addPolygon(vc + 3, vc + 2, vc + 1, vc);

                        vc = bricksMeshPart.vertices.size();
                        if(tLine.charAt(col - 1) == '*') {
                            bricksMeshPart.addVertex(x1, y1, z1, 0, 0, 1, 0, 0);
                            bricksMeshPart.addVertex(x1, y2, z1, 0, 1, 1, 0, 0);
                            bricksMeshPart.addVertex(x1, y2, z2, 2, 1, 1, 0, 0);
                            bricksMeshPart.addVertex(x1, y1, z2, 2, 0, 1, 0, 0);
                            bricksMeshPart.addPolygon(vc, vc + 1, vc + 2, vc + 3);
                            vc = bricksMeshPart.vertices.size();
                        }
                        if(tLine.charAt(col + 1) == '*') {
                            bricksMeshPart.addVertex(x2, y1, z1, 0, 0, -1, 0, 0);
                            bricksMeshPart.addVertex(x2, y2, z1, 0, 1, -1, 0, 0);
                            bricksMeshPart.addVertex(x2, y2, z2, 2, 1, -1, 0, 0);
                            bricksMeshPart.addVertex(x2, y1, z2, 2, 0, -1, 0, 0);
                            bricksMeshPart.addPolygon(vc + 3, vc + 2, vc + 1, vc);
                            vc = bricksMeshPart.vertices.size();
                        }
                        if(mapLines.get(row - 1).charAt(col) == '*') {
                            bricksMeshPart.addVertex(x1, y1, z1, 0, 0, 0, 0, 1);
                            bricksMeshPart.addVertex(x2, y1, z1, 2, 0, 0, 0, 1);
                            bricksMeshPart.addVertex(x2, y2, z1, 2, 1, 0, 0, 1);
                            bricksMeshPart.addVertex(x1, y2, z1, 0, 1, 0, 0, 1);
                            bricksMeshPart.addPolygon(vc, vc + 1, vc + 2, vc + 3);
                            vc = bricksMeshPart.vertices.size();
                        }
                        if(mapLines.get(row + 1).charAt(col) == '*') {
                            bricksMeshPart.addVertex(x1, y1, z2, 0, 0, 0, 0, -1);
                            bricksMeshPart.addVertex(x2, y1, z2, 2, 0, 0, 0, -1);
                            bricksMeshPart.addVertex(x2, y2, z2, 2, 1, 0, 0, -1);
                            bricksMeshPart.addVertex(x1, y2, z2, 0, 1, 0, 0, -1);
                            bricksMeshPart.addPolygon(vc + 3, vc + 2, vc + 1, vc);
                            vc = bricksMeshPart.vertices.size();
                        }
                    }
                }
            }
            stoneMeshPart.calcBounds();
            bricksMeshPart.calcBounds();
            mesh.calcBounds();

            node().renderable = mesh;

            System.out.println("map has been built successfully");
        } catch(Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
}
