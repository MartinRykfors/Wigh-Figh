uniform float time;
uniform vec2 size;

struct Ray
{
	vec3 org;
	vec3 dir;
};

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec2 mandelbox(vec3 p){
    float r = 0.8;
    vec3 c = vec3(sin(time) + p.x, sin(time*1.2), sin(time*1.98)) * 0.3;
    float scale = 1.5;
    float m = 1000.;
    for (float i = 0.; i < 1.; i += 1.){
        p = clamp(p,-1.,1.)*2. - p;
        float l = length(p);
        if (l < r){
            p /= r*r;
        }
        else if (l < 1.3){
            p /= l;
        }
        // float a = sin(time)*p.x*(3.-i)*0.1;
        // mat2 rot = mat2(cos(a), -sin(a), sin(a), cos(a));
        // p.zy *= rot;
        p = 1.2 * p + c;
        //m = min(m, length(p.xy + vec2(1.)));
    }
    return vec2(length(p), m);
}

vec3 quantize(vec3 p){
    p *= 1.;
    p -= fract(p);
    p /= 1.;
    return p;
}

vec3 render(Ray r){
    vec3 mapPos = vec3(floor(r.org));
    vec3 deltaDist = abs(vec3(length(r.dir)) / r.dir);
    ivec3 rayStep = ivec3(sign(r.dir));
    vec3 sideDist = (sign(r.dir) * (vec3(mapPos) - r.org) + (sign(r.dir) * 0.5) + 0.5) * deltaDist;
    bvec3 mask;
    vec3 col = vec3(0.);
    float t = 0.;
    for (int i = 0; i < 80; i++){
        bvec3 b1 = lessThan(sideDist.xyz, sideDist.yzx);
        bvec3 b2 = lessThan(sideDist.xyz, sideDist.zxy);
        mask.x = b1.x && b2.x;
        mask.y = b1.y && b2.y;
        mask.z = b1.z && b2.z;
        float f = 0.3/mandelbox(mapPos/2.).x;
        f = pow(f,4.);
        col += hsv2rgb(vec3(mapPos.z*0.02 + time, 0.9, f));
        sideDist += vec3(mask) * deltaDist;
        mapPos += vec3(mask) * vec3(rayStep);
    }
    return col;
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
	vec3 cameraPos = vec3(15.*sin(time/3.),15.*cos(time/3.),cos(time/5.) * 5.);
	vec3 lookAt = vec3(0.);
	vec3 up = vec3(0.,0.,1.);
	float aspect = size.x/size.y;
	Ray ray = createRay(cameraPos, lookAt, up, p, 90., aspect);
    vec3 col = render(ray);
    gl_FragColor = vec4(col, 1.);
}
