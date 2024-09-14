attribute vec3 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec3 aNormal;

varying vec3 vObjectPosition;
varying vec3 vPosition;
varying vec2 vTextureCoordinate;
varying vec3 vNormal;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

void main() {
    vec4 position = uModel * vec4(aPosition, 1.0);

    vObjectPosition = aPosition;
    vPosition = position.xyz;
    vTextureCoordinate = aTextureCoordinate;
    vNormal = normalize((uModelIT * vec4(aNormal, 0.0)).xyz);

    gl_Position = uProjection * uView * position;
}