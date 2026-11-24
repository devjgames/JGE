package org.game;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class GFX {
    
    public static void setDepthState(DepthState state) {
        if(state == DepthState.READWRITE) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        } else {
            GL11.glDepthMask(false);
            if(state == DepthState.NONE) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }
    }

    public static void setBlendState(BlendState state) {
        if(state == BlendState.OPAQUE) {
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            if(state == BlendState.ALPHA) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            }
        }
    }

    public static void setCullState(CullState state) {
        if(state == CullState.NONE) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
            if(state == CullState.BACK) {
                GL11.glCullFace(GL11.GL_BACK);
            } else {
                GL11.glCullFace(GL11.GL_FRONT);
            }
        }
    }

    public static void setViewport(int x, int y, int w, int h) {
        GL11.glViewport(x, y, w, h);
    }

    public static void clear(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
        setDepthState(DepthState.READWRITE);
        setBlendState(BlendState.OPAQUE);
        setCullState(CullState.BACK);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public static void clear(Vec4 color) {
        clear(color.x, color.y, color.z, color.w);
    }

    private static boolean hasError = false;

    public static void checkError(String tag) {
        if(!hasError) {
            int error = GL11.glGetError();

            if(error != GL11.GL_NO_ERROR) {
                hasError = true;
                Log.getInstance().put(1, tag + ":" + error);
            }
        }
    }

    static int getUsage(BufferUsage usage) {
        if(usage == BufferUsage.STATIC) {
            return GL15.GL_STATIC_DRAW;
        } else if(usage == BufferUsage.DYNAMIC) {
            return GL15.GL_DYNAMIC_DRAW;
        } else {
            return GL15.GL_STREAM_DRAW;
        }
    }

    static int getGeometryType(GeometryType type) {
        if(type == GeometryType.LINES) {
            return GL11.GL_LINES;
        } else {
            return GL11.GL_TRIANGLES;
        }
    }

    public static int stride(VertexAttribute ... attributes) {
        int stride = 0;

        for(VertexAttribute attribute : attributes) {
            stride += attribute.getSize();
        }
        return stride;
    }

    public static void validate(GeometryType type, int count, VertexAttribute ... attributes) throws Exception {
        int stride = stride(attributes);

        if(type != null) {
            if(type == GeometryType.LINES) {
                if(count / stride / 2 * stride * 2 != count) {
                    throw new Exception("invalid vertex count");
                } 
            } else if(count / stride / 3 * stride * 3 != count) {
                    throw new Exception("invalid vertex count");
            }
        } 
        if(count / stride * stride != count) {
            throw new Exception("invalid vertex stride");
        }
    }
}
