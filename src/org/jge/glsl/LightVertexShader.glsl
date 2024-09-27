#define MAX_LIGHTS 4

attribute vec3 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec3 aNormal;

varying vec3 vPosition;
varying vec2 vTextureCoordinate;
varying vec3 vNormal;
varying vec4 vShadowCoordinates[MAX_LIGHTS];

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;
uniform mat4 uLightProjection[MAX_LIGHTS];
uniform mat4 uLightView[MAX_LIGHTS];
uniform int uVertexLightCount;

void main() {
    vec4 position = uModel * vec4(aPosition, 1.0);

    vPosition = position.xyz;
    vTextureCoordinate = aTextureCoordinate;
    vNormal = normalize((uModelIT * vec4(aNormal, 0.0)).xyz);

    for(int i = 0; i != uVertexLightCount; i++) {
        vShadowCoordinates[i] = uLightProjection[i] * uLightView[i] * position;
    }

    gl_Position = uProjection * uView * position;
}