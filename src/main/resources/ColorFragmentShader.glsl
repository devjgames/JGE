#version 150

in vec2 vTextureCoordinate;
in vec4 vColor;

out vec4 oColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

void main() {
    vec4 color = vColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, vTextureCoordinate);
    }
    oColor = color;
}