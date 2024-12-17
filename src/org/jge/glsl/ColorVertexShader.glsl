attribute vec3 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec4 aColor;

varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vTextureCoordinate = aTextureCoordinate;
    vColor = aColor;

    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}