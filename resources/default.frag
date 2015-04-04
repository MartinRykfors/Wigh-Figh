uniform float time;
uniform vec2 size;

void main() {
    vec2 uv = gl_FragCoord.xy / size;
    
    gl_FragColor = vec4(sin(time), uv.x, uv.y, 1.);
}
