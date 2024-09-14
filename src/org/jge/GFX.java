package org.jge;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL2;

public class GFX {
    
    public static void setDepthState(DepthState state) {
        GL2 gl = Game.getGL();

        if(state == DepthState.NONE) {
            gl.glDisable(GL2.GL_DEPTH_TEST);
            gl.glDepthMask(false);
        } else {
            gl.glEnable(GL2.GL_DEPTH_TEST);
            if(state == DepthState.READONLY) {
                gl.glDepthMask(false);
            } else {
                gl.glDepthMask(true);
            }
        }
    }

    public static void setBlendState(BlendState state) {
        GL2 gl = Game.getGL();

        if(state == BlendState.OPAQUE) {
            gl.glDisable(GL2.GL_BLEND);
        } else {
            gl.glEnable(GL2.GL_BLEND);
            if(state == BlendState.ADDITIVE) {
                gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE);
            } else {
                gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    public static void setCullState(CullState state) {
        GL2 gl = Game.getGL();

        if(state == CullState.NONE) {
            gl.glDisable(GL2.GL_CULL_FACE);
        } else {
            gl.glEnable(GL2.GL_CULL_FACE);
            if(state == CullState.BACK) {
                gl.glCullFace(GL2.GL_BACK);
            } else {
                gl.glCullFace(GL2.GL_FRONT);
            }
        }
    }

    public static void clear(float r, float g, float b, float a) {
        GL2 gl = Game.getGL();

        setDepthState(DepthState.READWRITE);
        setCullState(CullState.BACK);
        setBlendState(BlendState.OPAQUE);
        gl.glClearColor(r, g, b, a);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }

    private static boolean hasError = false;

    public static void checkError(String tag) {
        if(!hasError) {
            GL2 gl = Game.getGL();
            int code = gl.glGetError();

            if(code != GL2.GL_NO_ERROR) {
                hasError = true;
                System.out.println(tag + ":" + code);
            }
        }
    }

    private static Matrix4f m = new Matrix4f();
    private static Vector4f v = new Vector4f();

    public static Vector3f unproject(float x, float y, float z, float vx, float vy, float vw, float vh, Matrix4f projection, Matrix4f view, Vector3f p) {
        m.set(projection).mul(view).invert();

        v.x = 2 * (x - vx) / vw - 1;
        v.y = 2 * (y - vy) / vh - 1;
        v.z = 2 * z - 1;
        v.mul(m, v);
        v.mul(1 / v.w);

        return p.set(v.x, v.y, v.z);
    }
}
