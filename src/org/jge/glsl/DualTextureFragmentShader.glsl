varying vec2 vTextureCoordinate;
varying vec2 vTextureCoordinate2;

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
        color *= texture2D(uTexture, vTextureCoordinate);
    }
    if(uTexture2Enabled != 0) {
        color *= texture2D(uTexture2, vTextureCoordinate2);
    }
    if(uDecalTextureEnabled != 0) {
        vec4 d = texture2D(uDecalTexture, vTextureCoordinate);

        color.rgb = (1.0 - d.a) * color.rgb + d.a * d.rgb;
    }
    gl_FragColor = color;
}