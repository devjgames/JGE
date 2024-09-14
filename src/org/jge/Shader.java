package org.jge;

import java.util.Hashtable;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL2;

public class Shader extends Resource {
    
    private final int program;
    private final int attributes;
    private final Hashtable<String, Integer> locations = new Hashtable<>();
    private final float[] matrix = new float[16];

    public Shader(byte[] vertexBytes, byte[] fragmentBytes, String ... attributes) throws Exception {
        GL2 gl = Game.getGL();
        int vs = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
        int fs = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
        int[] i = new int[1];


        this.attributes = attributes.length;

        gl.glShaderSource(vs, 1, new String[] { new String(vertexBytes) }, null);
        gl.glCompileShader(vs);

        gl.glShaderSource(fs, 1, new String[] { new String(fragmentBytes) }, null);
        gl.glCompileShader(fs);

        gl.glGetShaderiv(vs, GL2.GL_COMPILE_STATUS, i, 0);
        if(i[0] == 0) {
            gl.glGetShaderiv(vs, GL2.GL_INFO_LOG_LENGTH, i, 0);
            byte[] bytes = new byte[i[0]];
            gl.glGetShaderInfoLog(vs, i[0], null, 0, bytes, 0);
            System.out.println(new String(bytes, 0, bytes.length - 1));
            gl.glDeleteShader(vs);
            gl.glDeleteShader(fs);
            throw new Exception("failed to compile vertex shader");
        }
        gl.glGetShaderiv(fs, GL2.GL_COMPILE_STATUS, i, 0);
        if(i[0] == 0) {
            gl.glGetShaderiv(fs, GL2.GL_INFO_LOG_LENGTH, i, 0);
            byte[] bytes = new byte[i[0]];
            gl.glGetShaderInfoLog(fs, i[0], null, 0, bytes, 0);
            System.out.println(new String(bytes, 0, bytes.length - 1));
            gl.glDeleteShader(vs);
            gl.glDeleteShader(fs);
            throw new Exception("failed to compile fragment shader");
        }

        program = gl.glCreateProgram();
        gl.glAttachShader(program, vs);
        gl.glAttachShader(program, fs);
        gl.glDeleteShader(vs);
        gl.glDeleteShader(fs);
        for(int j = 0; j != attributes.length; j++) {
            gl.glBindAttribLocation(program, j, attributes[j]);
        }
        gl.glLinkProgram(program);
        gl.glGetProgramiv(program, GL2.GL_LINK_STATUS, i, 0);
        if(i[0] == 0) {
            gl.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, i, 0);
            byte[] bytes = new byte[i[0]];
            gl.glGetProgramInfoLog(program, i[0], null, 0, bytes, 0);
            System.out.println(new String(bytes, 0, bytes.length - 1));
            throw new Exception("failed to link shader program");
        }
    }

    public void begin() {
        GL2 gl = Game.getGL();

        gl.glUseProgram(program);
        for(int i = 0; i != attributes; i++) {
            gl.glEnableVertexAttribArray(i);
        }
    }

    public void end() {
        GL2 gl = Game.getGL();

        gl.glUseProgram(0);
        for(int i = 0; i != attributes; i++) {
            gl.glDisableVertexAttribArray(i);
        }
    }

    private int getLocation(String name) {
        GL2 gl = Game.getGL();

        if(!locations.containsKey(name)) {
            int l = gl.glGetUniformLocation(program, name);

            if(l < 0) {
                System.out.println("location '" + name + "' NOT FOUND!");
            } else {
                System.out.println("location '" + name + "' = " + l);
            }
            locations.put(name, l);
        }
        return locations.get(name);
    }

    public void set(String name, int value) {
        Game.getGL().glUniform1i(getLocation(name), value);
    }

    public void set(String name, boolean value) {
        set(name, (value) ? 1 : 0);
    }

    public void set(String name, float value) {
        Game.getGL().glUniform1f(getLocation(name), value);
    }

    public void set(String name, float x, float y) {
        Game.getGL().glUniform2f(getLocation(name), x, y);
    }

    public void set(String name, Vector2f value) {
        set(name, value.x, value.y);
    }

    public void set(String name, float x, float y, float z) {
        Game.getGL().glUniform3f(getLocation(name), x, y, z);
    }

    public void set(String name, Vector3f value) {
        set(name, value.x, value.y, value.z);
    }

    public void set(String name, float x, float y, float z, float w) {
        Game.getGL().glUniform4f(getLocation(name), x, y, z, w);
    }

    public void set(String name, Vector4f value) {
        set(name, value.x, value.y, value.z, value.w);
    }

    public void set(String name, Matrix4f value) {
        Game.getGL().glUniformMatrix4fv(getLocation(name), 1, false, value.get(matrix), 0);
    }

    public void bind(int target, String name, int unit, int id) {
        GL2 gl = Game.getGL();

        gl.glActiveTexture(GL2.GL_TEXTURE0 + unit);
        set(name, unit);
        gl.glBindTexture(target, id);
    }
    
    @Override
    public void destroy() throws Exception {
        Game.getGL().glDeleteProgram(program);
        super.destroy();
    }
}
