attribute vec3 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec2 aTextureCoordinate2;

varying vec2 vTextureCoordinate;
varying vec2 vTextureCoordinate2;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vTextureCoordinate = aTextureCoordinate;
    vTextureCoordinate2 = aTextureCoordinate2;

    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}