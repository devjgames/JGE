attribute vec3 aPosition;

varying vec3 vPosition;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vec4 position = uModel * vec4(aPosition, 1.0);

    vPosition = position.xyz;

    gl_Position = uProjection * uView * position;
}