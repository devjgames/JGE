varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

void main() {
    vec4 color = vColor;

    if(uTextureEnabled != 0) {
        color *= texture2D(uTexture, vTextureCoordinate);
    }
    gl_FragColor = color;
}