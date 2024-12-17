varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform sampler2D uTexture;

void main() {
    gl_FragColor = vColor * texture2D(uTexture, vTextureCoordinate);
}
