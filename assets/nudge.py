import bpy

# usage
#
# 1) in blender reset texture coordinates on geometry quads to fit texture
#
# 2) in blender paste this code into python console
#
# 3) in blender select the mesh to nudge
#
# 4) in blender python console run ...
# nudge(pixels / textureSize, dotProductCutoff)
#

class Edge:
    def __init__(self, index):
        self.index = index
        self.pair = -1
        self.next = -1
        self.vertex = -1
        self.face = -1
        self.textureCoordinate = None

class Face:
    def __init__(self, index):
        self.index = index
        self.edge = -1
        self.polygon = -1

def nudge(amount, cutoff):
    mesh = bpy.context.object.data
    edges = [] 
    vertices = []
    faces = []
    edgePairs = {}
    for i in range(0, len(mesh.polygons)):
        p = mesh.polygons[i]
        if len(p.loop_indices) == 4:
            face = Face(len(faces))
            face.polygon = i
            for j in range(0, 4):
                edge = Edge(len(edges))
                edge.vertex = mesh.loops[p.loop_indices[j]].vertex_index
                edge.face = face.index
                edge.textureCoordinate = mesh.uv_layers[0].uv[p.loop_indices[j]].vector
                if j == 0:
                    face.edge = edge.index
                else:
                    edges[edge.index - 1].next = edge.index
                    edge.prev = edge.index - 1
                    if j == 3:
                        edge.next = face.edge
                edges.append(edge)
            faces.append(face)
            for j in range(0, 4):
                v1 = edges[face.edge + j].vertex
                v2 = edges[face.edge + ((j + 1) % 4)].vertex
                if v1 > v2:
                    temp = v1
                    v1 = v2
                    v2 = temp
                key = str(v1) + ":" + str(v2)
                edge = edges[face.edge + j]
                if key in edgePairs:
                    pair = edges[edgePairs[key]]
                    if pair.pair != -1:
                        print("already has a pair")
                    else:
                        pair.pair = edge.index
                        edge.pair = pair.index
                else:
                    edgePairs[key] = edge.index
        else:
            print("not quad geometry")
    for edge in edges:
        f1 = faces[edge.face]
        if edge.pair == -1:
            print("pair not found")
            continue
        f2 = faces[edges[edge.pair].face]
        p1 = mesh.polygons[f1.polygon]
        p2 = mesh.polygons[f2.polygon]
        n1 = p1.normal
        n2 = p2.normal
        if n1.x * n2.x + n1.y * n2.y + n1.z * n2.z > cutoff:
            t1 = edge.textureCoordinate
            t2 = edges[edge.next].textureCoordinate
            if abs(t1.x - t2.x) < 0.0000001:
                if t1.x < 0.0000001:
                    t1.x += amount 
                    t2.x += amount
                else:
                    t1.x -= amount
                    t2.x -= amount
            elif abs(t1.y - t2.y) < 0.0000001:
                if t1.y < 0.0000001:
                    t1.y += amount
                    t2.y += amount
                else:
                    t1.y -= amount
                    t2.y -= amount
            else:
                print("tex " + str(t1.x) + ", " + str(t1.y) + " -> " + str(t2.x) + ", " + str(t2.y))

    
