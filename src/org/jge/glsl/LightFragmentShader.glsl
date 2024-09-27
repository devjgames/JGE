#define MAX_LIGHTS 4

varying vec3 vPosition;
varying vec2 vTextureCoordinate;
varying vec3 vNormal;
varying vec4 vShadowCoordinates[MAX_LIGHTS];

uniform sampler2D uTexture;
uniform int uTextureEnabled;
uniform sampler2D uDecalTexture;
uniform int uDecalTextureEnabled;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];
uniform int uLightIsSpot[MAX_LIGHTS];
uniform float uLightSpotCutOff[MAX_LIGHTS];
uniform vec3 uLightSpotDirection[MAX_LIGHTS];
uniform float uLightShadowOffset[MAX_LIGHTS];
uniform sampler2D uLightShadowMap[MAX_LIGHTS];
uniform vec2 uLightShadowMapPixelSize[MAX_LIGHTS];

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

uniform int uReceivesShadow;

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

        if(uLightIsSpot[i] != 0) {
            float shadowValue = 1.0;

            if(uReceivesShadow != 0) {
                vec2 shadowCoord = vShadowCoordinates[i].xy / vShadowCoordinates[i].w;

                shadowCoord *= 0.5;
                shadowCoord += vec2(0.5, 0.5);
                shadowValue = 0.0;
                if(shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0) {
                    float shadowFactor = 0.25;

                    shadowValue += float(texture2D(uLightShadowMap[i], shadowCoord + uLightShadowMapPixelSize[i] * vec2(-2.0, -2.0)).x + uLightShadowOffset[i] > length(lightOffset)) * shadowFactor;
                    shadowValue += float(texture2D(uLightShadowMap[i], shadowCoord + uLightShadowMapPixelSize[i] * vec2(+2.0, -2.0)).x + uLightShadowOffset[i] > length(lightOffset)) * shadowFactor;
                    shadowValue += float(texture2D(uLightShadowMap[i], shadowCoord + uLightShadowMapPixelSize[i] * vec2(+2.0, +2.0)).x + uLightShadowOffset[i] > length(lightOffset)) * shadowFactor;
                    shadowValue += float(texture2D(uLightShadowMap[i], shadowCoord + uLightShadowMapPixelSize[i] * vec2(-2.0, +2.0)).x + uLightShadowOffset[i] > length(lightOffset)) * shadowFactor;
                }
            }
            atten *= clamp(dot(normalize(uLightSpotDirection[i]), -lightNormal) - uLightSpotCutOff[i], 0.0, 1.0) / (1.0 - uLightSpotCutOff[i]);
            atten *= shadowValue;
        }
        color += atten * lDotN * diffuseColor * uLightColor[i];
    }

    vec2 coord = vTextureCoordinate;

    if(uTextureEnabled != 0) {
        color *= texture2D(uTexture, coord);
    }
    if(uDecalTextureEnabled != 0) {
        vec4 d = texture2D(uDecalTexture, coord);

        color.rgb = (1.0 - d.a) * color.rgb + d.a * d.rgb;
    }
    gl_FragColor = color;
}
