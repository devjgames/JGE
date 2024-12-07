#version 150

in vec3 aPosition;
in vec2 aTextureCoordinate;
in vec2 aTextureCoordinate2;

out vec2 vTextureCoordinate;
out vec2 vTextureCoordinate2;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vTextureCoordinate = aTextureCoordinate;
    vTextureCoordinate2 = aTextureCoordinate2;

    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}