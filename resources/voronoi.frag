uniform float time;
uniform vec2 size;

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float hash(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec2 voronoi(vec2 p, out vec2 pointA, out vec2 pointB){
    vec2 minDist = vec2(100.);
    vec2 n = floor(p);
    vec2 f = fract(p);

    for (int i = -1; i<=1; i++ ){
        for (int j = -1; j<=1; j++ ){
            vec2 ii = vec2(float(i), float(j));
            vec2 point = ii - f;
            point += vec2(hash(n+ii),hash(n+ii+vec2(1.,0.)))*.7;
            point.x += 0.3*sin(time - hash(n+ii)*14.);
            //float d = max(abs(delta.x) , abs(delta.y));
            float d = dot(point,point);
            // float d = abs(delta.x) + abs(delta.y);
            if (d < minDist.x){
                minDist.y = minDist.x;
                minDist.x = d;
                pointA = point;
            } else if (d < minDist.y){
                minDist.y = d;
                pointB = point;
            }
        }
    }
    return sqrt(minDist);
}

void main() {
    vec2 uv = gl_FragCoord.xy / size;
    vec2 pointA, pointB;
    vec2 v = voronoi(uv*4., pointA, pointB);
    float dis = dot(0.5*(pointA+pointB), normalize(pointB-pointA));
    float intensity = dis*(1.+sin(dis*90.))/2.;
    float border = 1.-smoothstep(0.01,0.1,dis);
    vec3 col = hsv2rgb(vec3(0.0+border, 0.6, intensity + border));
    //col = vec3(1.) * mod(v.y*3.23+v.z*2.21,1.);
    gl_FragColor = vec4(col,1.);
}
