package org.jge;

import java.util.Hashtable;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class Shader extends Resource {
    
    private final int program;
    private final int attributes;
    private final Hashtable<String, Integer> locations = new Hashtable<>();
    private final float[] matrix = new float[16];

    public Shader(byte[] vertexBytes, byte[] fragmentBytes, String ... attributes) throws Exception {
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        int i;


        this.attributes = attributes.length;

        GL20.glShaderSource(vs, new String(vertexBytes));
        GL20.glCompileShader(vs);

        GL20.glShaderSource(fs, new String(fragmentBytes));
        GL20.glCompileShader(fs);

        i = GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS);
        if(i == 0) {
            System.out.println(GL20.glGetShaderInfoLog(vs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile vertex shader");
        }
        i = GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS);
        if(i == 0) {
            System.out.println(GL20.glGetShaderInfoLog(fs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile fragment shader");
        }

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
        for(int j = 0; j != attributes.length; j++) {
            GL20.glBindAttribLocation(program, j, attributes[j]);
        }
        GL20.glLinkProgram(program);
        i = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if(i == 0) {
            System.out.println(GL20.glGetProgramInfoLog(program));
            throw new Exception("failed to link shader program");
        }
    }

    public void begin() {
        GL20.glUseProgram(program);
        for(int i = 0; i != attributes; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    public void end() {
        GL20.glUseProgram(0);
        for(int i = 0; i != attributes; i++) {
            GL20.glDisableVertexAttribArray(i);
        }
    }

    private int getLocation(String name) {
        if(!locations.containsKey(name)) {
            int l = GL20.glGetUniformLocation(program, name);

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
        GL20.glUniform1i(getLocation(name), value);
    }

    public void set(String name, boolean value) {
        set(name, (value) ? 1 : 0);
    }

    public void set(String name, float value) {
        GL20.glUniform1f(getLocation(name), value);
    }

    public void set(String name, float x, float y) {
       GL20.glUniform2f(getLocation(name), x, y);
    }

    public void set(String name, Vector2f value) {
        set(name, value.x, value.y);
    }

    public void set(String name, float x, float y, float z) {
        GL20.glUniform3f(getLocation(name), x, y, z);
    }

    public void set(String name, Vector3f value) {
        set(name, value.x, value.y, value.z);
    }

    public void set(String name, float x, float y, float z, float w) {
        GL20.glUniform4f(getLocation(name), x, y, z, w);
    }

    public void set(String name, Vector4f value) {
        set(name, value.x, value.y, value.z, value.w);
    }

    public void set(String name, Matrix4f value) {
        GL20.glUniformMatrix4fv(getLocation(name), false, value.get(matrix));
    }

    public void bind(int target, String name, int unit, int id) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        set(name, unit);
        GL20.glBindTexture(target, id);
    }
    
    @Override
    public void destroy() throws Exception {
        GL20.glDeleteProgram(program);
        super.destroy();
    }
}
