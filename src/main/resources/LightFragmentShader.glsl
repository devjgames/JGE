#version 150

in vec2 vTextureCoordinate;
in vec4 vColor;

out vec4 oColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uDecalTexture;
uniform int uDecalTextureEnabled;

void main() {
    vec4 color = vColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, vTextureCoordinate);
    }
    if(uDecalTextureEnabled != 0) {
        vec4 d = texture(uDecalTexture, vTextureCoordinate);

        color.rgb = (1.0 - d.a) * color.rgb + d.a * d.rgb;
    }
    oColor = color;
}
