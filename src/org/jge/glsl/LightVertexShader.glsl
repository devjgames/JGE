#define MAX_LIGHTS 16

attribute vec3 aPosition;
attribute vec2 aTextureCoordinate;
attribute vec3 aNormal;

varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform int uLightCount;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

void main() {
    vec4 position = uModel * vec4(aPosition, 1.0);
    vec3 normal = normalize((uModelIT * vec4(aNormal, 0.0)).xyz);
    vec4 color = uAmbientColor;

    for(int i = 0; i != uLightCount; i++) {
        vec3 offset = uLightPosition[i] - position.xyz;
        vec3 lightNormal = normalize(offset);
        float lDotN = clamp(dot(lightNormal, normal), 0.0, 1.0);
        float atten = 1.0 - clamp(length(offset) / uLightRadius[i], 0.0, 1.0);

        color += lDotN * atten * uDiffuseColor * uLightColor[i];
    }
    vTextureCoordinate = aTextureCoordinate;
    vColor = color;

    gl_Position = uProjection * uView * position;
}