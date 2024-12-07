#version 150

in vec2 aPosition;
in vec2 aTextureCoordinate;
in vec4 aColor;

out vec2 vTextureCoordinate;
out vec4 vColor;

uniform mat4 uProjection;

void main() {
     vTextureCoordinate = aTextureCoordinate;
     vColor = aColor;

     gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);
}