varying vec2 vTextureCoordinate;
varying vec4 vColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uDecalTexture;
uniform int uDecalTextureEnabled;

void main() {
    vec4 color = vColor;

    if(uTextureEnabled != 0) {
        color *= texture2D(uTexture, vTextureCoordinate);
    }
    if(uDecalTextureEnabled != 0) {
        vec4 d = texture2D(uDecalTexture, vTextureCoordinate);

        color.rgb = (1.0 - d.a) * color.rgb + d.a * d.rgb;
    }
    gl_FragColor = color;
}
