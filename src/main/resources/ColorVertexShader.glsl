#version 150

in vec3 aPosition;
in vec2 aTextureCoordinate;
in vec4 aColor;

out vec2 vTextureCoordinate;
out vec4 vColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vTextureCoordinate = aTextureCoordinate;
    vColor = aColor;

    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}