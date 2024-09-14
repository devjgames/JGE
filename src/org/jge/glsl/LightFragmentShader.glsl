#define MAX_LIGHTS 8

varying vec3 vObjectPosition;
varying vec3 vPosition;
varying vec2 vTextureCoordinate;
varying vec3 vNormal;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

uniform int uLightCount;

void main() {
    vec4 color = uAmbientColor;
    vec3 normal = normalize(vNormal);
    vec3 position = vPosition;

    for(int i = 0; i != uLightCount; i++) {
        vec3 lightOffset = uLightPosition[i] - position;
        vec3 lightNormal = normalize(lightOffset);
        float lDotN = clamp(dot(lightNormal, normal), 0.0, 1.0);
        float atten = 1.0 - clamp(length(lightOffset) / uLightRadius[i], 0.0, 1.0);
        vec4 diffuseColor = uDiffuseColor;

        color += atten * lDotN * diffuseColor * uLightColor[i];
    }

    vec2 coord = vTextureCoordinate;

    if(uTextureEnabled != 0) {
        color *= texture2D(uTexture, coord);
    }
    gl_FragColor = color;
}
