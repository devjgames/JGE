attribute vec2 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec4 aColor;

varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform mat4 uProjection;

void main() {
     vTextureCoordinate = aTextureCoordinate;
     vColor = aColor;

     gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);
}