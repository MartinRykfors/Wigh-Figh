uniform float time;
uniform vec2 size;
#define PI 3.141592653589

struct Ray
{
	vec3 org;
	vec3 dir;
};

mat3 rotateX(float a){
    return mat3(1.,0.,0.,
                0.,cos(a), -sin(a),
                0.,sin(a), cos(a));
}

mat3 rotateY(float a){
    return mat3(cos(a), 0., -sin(a),
                0.,1.,0.,
                sin(a), 0., cos(a));
}

mat3 rotation;

float hash(float f)
{
    return fract(sin(f*32.34182) * 43758.5453);
}

float hash(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 background(vec3 dir){
    float f = dir.z + 1.;
    return vec3(f/2.);
}

float box(vec3 p, vec3 w){
    p.z -= 2.;
    p.x += sin(p.y);
    p.y += sin(p.z);
    p.z += sin(p.x);
    return length(p) - 3.;
    // p = abs(p);
    // float m = max(p.x-w.x, max(p.y-w.y, p.z-w.z));
    // return m;
}

float map(vec3 p){
    //float boxDist = box(p, vec3(0.5,0.5,5.));
    float i = sin(p.x)+1.;
    i *= 3. * sin(time + p.y);
    p.x += sin(p.y*.3);
    p.y += sin(p.x);
    p.x += sin(p.y*0.8+time);
    p.y += sin(p.x*4.3);
    p.z += sin(p.y + sin(p.x))*0.1 * i;
    float planeDist = abs(p.z) - 1.3;
    return planeDist;
    //return min(boxDist, planeDist);
}

vec3 normal(vec3 pos)
{
	vec3 eps = vec3( 0.001, 0.0, 0.0 );
	vec3 nor = vec3(map(pos+eps.xyy) - map(pos-eps.xyy),
                    map(pos+eps.yxy) - map(pos-eps.yxy),
                    map(pos+eps.yyx) - map(pos-eps.yyx) );
	return normalize(nor);
}

float shadow(vec3 pos, vec3 ldir){
    float t = 0.05;
    float res = 1.0;
    for (int i = 0; i < 100; i++){
        float h = map(pos+t*ldir);
        res = min(res, 10.*h/t);
        t+=h*.1;
    }
    return res;
}

vec3 render(Ray ray){
    float dist = 0.;
    vec3 pos;
    float curMap;
    for (int i = 0; i < 200; i++){
        pos = ray.org + dist*ray.dir;
        curMap = map(pos);
        dist+=curMap*.1;
    }
    float m = map(pos);
    if (m < 0.002){
        vec3 l = vec3(sin(time), 0., 0.8)*8.;
        vec3 ld = normalize(l-pos);
        vec3 n = normal(pos);
        vec3 r = reflect(ray.dir, n);
        //return vec3(fract(pos.x-0.1));
        float s = shadow(pos,ld);
        float spec = pow(clamp(dot(r,ld),0.,1.),55.);
        return (vec3(0.8)*clamp(dot(n,ld),0.,1.) + spec)*s;
    }
    return background(ray.dir);
}

Ray createRay(vec3 center, vec3 lookAt, vec3 up, vec2 uv, float fov, float aspect)
{
	Ray ray;
	ray.org = center;
	vec3 dir = normalize(lookAt - center);
	up = normalize(up - dir*dot(dir,up));
	vec3 right = cross(dir, up);
	uv = 2.*uv - vec2(1.);
	fov = fov * 3.1415/180.;
	ray.dir = dir + tan(fov/2.) * right * uv.x + tan(fov/2.) / aspect * up * uv.y;
	ray.dir = normalize(ray.dir);	
	return ray;
}

void main(){
    vec2 p = gl_FragCoord.xy / size;
	vec3 cameraPos = vec3(2.,2.*sin(time/10.),9.);
	vec3 lookAt = vec3(0.);
	vec3 up = vec3(0.,0.,1.);
	float aspect = size.x/size.y;
	Ray ray = createRay(cameraPos, lookAt, up, p, 90., aspect);
    vec3 col = render(ray);
    gl_FragColor = vec4(col, 1.);
}
