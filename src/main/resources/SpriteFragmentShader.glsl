#version 150

in vec2 vTextureCoordinate;
in vec4 vColor;

out vec4 oColor;

uniform sampler2D uTexture;

void main() {
    oColor = vColor * texture(uTexture, vTextureCoordinate);
}
