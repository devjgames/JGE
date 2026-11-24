#version 150 core

// Post-process toon shading:
//  - color quantization (posterized bands, applied to luminance so hue is preserved)
//  - Sobel edge detection on scene luminance for ink outlines

uniform sampler2D uColorTex;    // scene color

uniform vec2  uTexelSize;       // 1.0 / resolution

uniform int   uBands;           // number of shading bands (e.g. 4)
uniform float uBandSoftness;    // 0.0 = hard steps, ~0.05 = slightly smoothed
uniform float uSaturation;      // 1.0 = unchanged, >1 more vivid

uniform float uEdgeWidth;       // outline thickness in pixels (e.g. 1.0)
uniform float uEdgeThreshold;   // e.g. 0.3
uniform vec3  uOutlineColor;    // e.g. vec3(0.05, 0.03, 0.08)

in vec2 vTexCoord;
out vec4 fragColor;

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

// Quantize a 0..1 value into N bands, with optional smoothing at band edges.
float quantize(float v, int bands, float softness)
{
    float n = float(max(bands, 1));
    float x = clamp(v, 0.0, 1.0) * n;
    float base = floor(x);
    float f = x - base;
    float t = (softness <= 0.0)
        ? 0.0
        : smoothstep(0.5 - softness, 0.5 + softness, f);
    return clamp((base + t) / n, 0.0, 1.0);
}

// Sobel magnitude over a 3x3 neighborhood of scalar samples.
float sobel(float tl, float t, float tr,
            float l,           float r,
            float bl, float b, float br)
{
    float gx = -tl - 2.0 * l - bl + tr + 2.0 * r + br;
    float gy = -tl - 2.0 * t - tr + bl + 2.0 * b + br;
    return sqrt(gx * gx + gy * gy);
}

void main()
{
    vec2 uv = vTexCoord;
    vec3 scene = texture(uColorTex, uv).rgb;

    // ---------------- Toon color banding ----------------
    float lum = dot(scene, LUMA);
    float qLum = quantize(lum, uBands, uBandSoftness);

    // Rescale color so its luminance lands on the quantized value (keeps hue).
    vec3 toon = (scene / max(lum, 1e-4)) * qLum;

    // Saturation tweak
    float tl = dot(toon, LUMA);
    toon = mix(vec3(tl), toon, uSaturation);
    toon = clamp(toon, 0.0, 1.0);

    // ---------------- Edge detection (luminance Sobel) ----------------
    vec2 o = uTexelSize * uEdgeWidth;

    float c0 = dot(texture(uColorTex, uv + vec2(-o.x,  o.y)).rgb, LUMA);
    float c1 = dot(texture(uColorTex, uv + vec2( 0.0,  o.y)).rgb, LUMA);
    float c2 = dot(texture(uColorTex, uv + vec2( o.x,  o.y)).rgb, LUMA);
    float c3 = dot(texture(uColorTex, uv + vec2(-o.x,  0.0)).rgb, LUMA);
    float c4 = dot(texture(uColorTex, uv + vec2( o.x,  0.0)).rgb, LUMA);
    float c5 = dot(texture(uColorTex, uv + vec2(-o.x, -o.y)).rgb, LUMA);
    float c6 = dot(texture(uColorTex, uv + vec2( 0.0, -o.y)).rgb, LUMA);
    float c7 = dot(texture(uColorTex, uv + vec2( o.x, -o.y)).rgb, LUMA);

    float edge = step(uEdgeThreshold, sobel(c0, c1, c2, c3, c4, c5, c6, c7));

    // ---------------- Composite ----------------
    fragColor = vec4(mix(toon, uOutlineColor, edge), 1.0);
}
