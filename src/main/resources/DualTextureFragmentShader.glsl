#version 150

in vec2 vTextureCoordinate;
in vec2 vTextureCoordinate2;

out vec4 oColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uTexture2;
uniform int uTexture2Enabled;

uniform sampler2D uDecalTexture;
uniform int uDecalTextureEnabled;

uniform vec4 uColor;

void main() {
    vec4 color = uColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, vTextureCoordinate);
    }
    if(uTexture2Enabled != 0) {
        color *= texture(uTexture2, vTextureCoordinate2);
    }
    if(uDecalTextureEnabled != 0) {
        vec4 d = texture(uDecalTexture, vTextureCoordinate);

        color.rgb = (1.0 - d.a) * color.rgb + d.a * d.rgb;
    }
    oColor = color;
}