package org.game;

import java.util.Hashtable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Shader extends Resource {
    
    private int program = 0;
    private int vao = 0;
    private VertexAttribute[] attributes = null;
    private Hashtable<String, Integer> uniforms = new Hashtable<>();
    private float[] matrixArray = new float[16];

    public Shader(String vertexSource, String fragmentSource, VertexAttribute ... attributes) throws Exception {
        this(vertexSource, fragmentSource, null, attributes);
    }

    public Shader(String vertexSource, String fragmentSource, String[] gbuffers, VertexAttribute ... attributes) throws Exception {
        Log.getInstance().put(1, "creating shader. ...");
        
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        GL20.glShaderSource(vs, vertexSource);
        GL20.glCompileShader(vs);

        GL20.glShaderSource(fs, fragmentSource);
        GL20.glCompileShader(fs);

        if(GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            Log.getInstance().put(1, vertexSource);
            Log.getInstance().put(1, GL20.glGetShaderInfoLog(vs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile vertex shader");
        }

        if(GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            Log.getInstance().put(1, fragmentSource);
            Log.getInstance().put(1, GL20.glGetShaderInfoLog(fs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile fragment shader");
        }

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        this.attributes = attributes;
        for(int i = 0; i != attributes.length; i++) {
            GL20.glBindAttribLocation(program, i, attributes[i].getName());
        }
        if(gbuffers != null) {
            for(int i = 0; i != gbuffers.length; i++) {
                GL30.glBindFragDataLocation(program, i, gbuffers[i]);
            }
        }
        GL20.glLinkProgram(program);
        if(GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            Log.getInstance().put(1, vertexSource);
            Log.getInstance().put(1, fragmentSource);
            Log.getInstance().put(1, GL20.glGetProgramInfoLog(program));
            GL20.glDeleteProgram(program);
            program = 0;
            throw new Exception("failed link shader program");
        }
        vao = GL30.glGenVertexArrays();
    }

    public int getAttributeCount() {
        return attributes.length;
    }

    public VertexAttribute getAttribute(int i) {
        return attributes[i];
    }

    public int getStride() {
        return GFX.stride(attributes);
    }

    public void begin() {
        GL30.glBindVertexArray(vao);
        GL20.glUseProgram(program);
        for(int i = 0; i != attributes.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    private int getUniform(String name) {
        if(!uniforms.containsKey(name)) {
            int i = GL20.glGetUniformLocation(program, name);

            if(i < 0) {
                Log.getInstance().put(1, "uniform '" + name + "' NOT FOUND!");
            } else {
                Log.getInstance().put(1, "uniform '" + name + "' = " + i);
            }
            uniforms.put(name, i);
        }
        return uniforms.get(name);
    }

    public void set(String name, int x) {
        GL20.glUniform1i(getUniform(name), x);
    }

    public void set(String name, boolean x) {
        set(name, (x) ? 1 : 0);
    }

    public void set(String name, float x) {
        GL20.glUniform1f(getUniform(name), x);
    }

    public void set(String name, float x, float y) {
        GL20.glUniform2f(getUniform(name), x, y);
    }

    public void set(String name, Vec2 v) {
        set(name, v.x, v.y);
    }

    public void set(String name, float x, float y, float z) {
        GL20.glUniform3f(getUniform(name), x, y, z);
    }

    public void set(String name, Vec3 v) {
        set(name, v.x, v.y, v.z);
    }

    public void set(String name, float x, float y, float z, float w) {
        GL20.glUniform4f(getUniform(name), x, y, z, w);
    }

    public void set(String name, Vec4 v) {
        set(name, v.x, v.y, v.z, v.w);
    }

    public void set(String name, Mat4 m) {
        m.get(matrixArray);
        GL20.glUniformMatrix4fv(getUniform(name), false, matrixArray);
    }

    public void set(String name, Texture texture, int unit) {
        GL20.glActiveTexture(GL20.GL_TEXTURE0 + unit);
        GL20.glUniform1i(getUniform(name), unit);
        texture.bind();
    }

    public void draw(GeometryType type, VertexArrayBuffer vertexBuffer) throws Exception {
        if(vertexBuffer.getCount() != 0) {
            int x = 0;

            GFX.validate(type, vertexBuffer.getCount(), attributes);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer.getVBO());
            for(int i = 0; i != attributes.length; i++) {
                VertexAttribute attribute = attributes[i];

                GL20.glVertexAttribPointer(i, attribute.getSize(), GL11.GL_FLOAT, false, GFX.stride(attributes) * 4, x);
                x += attribute.getSize() * 4;
            }
            GL15.glDrawArrays(GFX.getGeometryType(type), 0, vertexBuffer.getCount() / GFX.stride(attributes));
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }
    }

    public void draw(GeometryType type, VertexArrayBuffer vertexBuffer, VertexElementBuffer vertexElementBuffer) throws Exception {
        if(vertexBuffer.getCount() != 0 && vertexElementBuffer.getCount() != 0) {
            int x = 0;

            GFX.validate(null, vertexElementBuffer.getCount() * GFX.stride(attributes), attributes);
            vertexElementBuffer.validate(vertexBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer.getVBO());
            for(int i = 0; i != attributes.length; i++) {
                VertexAttribute attribute = attributes[i];

                GL20.glVertexAttribPointer(i, attribute.getSize(), GL11.GL_FLOAT, false, GFX.stride(attributes) * 4, x);
                x += attribute.getSize() * 4;
            }
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexElementBuffer.getVEO());
            GL15.glDrawElements(GFX.getGeometryType(type), vertexElementBuffer.getCount(), GL11.GL_UNSIGNED_INT, 0);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }
    }

    public void end() {
        for(int i = 0; i != attributes.length; i++) {
            GL20.glDisableVertexAttribArray(i);
        }
        GL20.glUseProgram(0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        GL20.glDeleteProgram(program);
        GL30.glDeleteVertexArrays(vao);
        super.destroy();
    }
}
