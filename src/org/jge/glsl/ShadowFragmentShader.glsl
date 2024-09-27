
varying vec3 vPosition;

uniform vec3 uLightPosition;

void main() {
    gl_FragColor = vec4(length(uLightPosition - vPosition), 0.0, 0.0, 1.0);
}