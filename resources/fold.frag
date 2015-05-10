uniform float time;
uniform vec2 size;
float atime;

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

vec3 background(vec3 dir){
    float a = atan(dir.y, dir.x);
    float f = dir.z;
    vec3 nadir = vec3(.1,.3,.5);
    vec3 ground = vec3(.1,.6,.2);
    vec3 sky = vec3(1.);
    vec3 zenith = vec3(.0, .0, .3);
    vec3 col = f < 0. ? mix(nadir, ground, f+1.) : mix(sky, zenith, pow(f,.25));
    return col * (5.+sin(a*2.))/6.*2.5;
}

vec4 box(vec3 p, float w){
    p = abs(p);
    float dx = p.x-w;
    float dy = p.y-w;
    float dz = p.z-w;
    float m = max(p.x-w, max(p.y-w, p.z-w));
    return vec4(m,dx,dy,dz);
}

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

vec4 map(vec3 p){
    float e = 0.1;
    // float a = p.z*sin(atime*3.)*0.6;
    // p.xy *= mat2(cos(a), -sin(a), sin(a), cos(a));
    for (int i = 0; i < 5; i++){
        p = abs(p*rotation);
        //p.xyz = p.zyx;
        p.y -= .5;
        //p.x -= e*2.;
        p.z -= e;
        e*=sin(time*80.)*2.0*pow((1.-fract(time)),4.);
    }
    return box(p, .7);
}

vec3 normal(vec3 pos)
{
	vec3 eps = vec3( 0.001, 0.0, 0.0 );
	vec3 nor = vec3(
	    map(pos+eps.xyy).x - map(pos-eps.xyy).x,
	    map(pos+eps.yxy).x - map(pos-eps.yxy).x,
	    map(pos+eps.yyx).x - map(pos-eps.yyx).x );
	return normalize(nor);
}

float ao(vec3 pos, vec3 n){
    float d = 0.09;
    float sum = 0.;
    float fac = 1.;
    for (int i = 1; i < 5; i++){
        sum += d*float(i) - map(pos + n*d*float(i)).x/fac;
        fac *= 2.;
    }
    return 1.-sum;
}

vec3 render(Ray ray){
    float dist = 0.;
    vec3 pos;
    for (int i = 0; i < 60; i++){
        pos = ray.org + dist*ray.dir;
        dist+=map(pos).x;
    }
    vec4 m = map(pos);
    if (m.x < 0.01){
        vec3 n = normal(pos);
        vec3 l = normalize(vec3(1.,2.,5.));
        vec3 diffuse = clamp(dot(n, l),0., 1.)*vec3(1.);
        vec3 r = reflect(ray.dir, n);
        vec3 refl = background(r);
        float dx = m.y;
        float dy = m.z;
        float dz = m.w;
        float start = 0.02;
        float end = 0.023;
        float f = smoothstep(start, end, abs(dx-dy));
        f *= smoothstep(start, end, abs(dx-dz));
        f *= smoothstep(start, end, abs(dz-dy));
        f = 1. - f;
        // float flash = pow((1.+sin(time*20.))/2.,10.);
        float a = ao(pos, n);
        float rf = 1.-abs(dot(ray.dir, n));
        return diffuse*a*(1.-rf) + 0.2*a + f + refl*rf; 
    }

    //return vec3(0.);
    return background(ray.dir)*0.3;
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
	vec3 cameraPos = vec3(5.*sin(time/3.),5.*cos(time/3.),-3.);
	vec3 lookAt = vec3(0.);
	vec3 up = vec3(0.,0.,1.);
	float aspect = size.x/size.y;
    float t = floor(time);
    float f = fract(time);
    t += 1. - exp(-f*9.);
    atime = t;
    rotation = rotateX(atime*1.9)*rotateY(atime*1.4);
	Ray ray = createRay(cameraPos, lookAt, up, p, 90., aspect);
    vec3 col = render(ray);
    float vig = p.x*(1.-p.x)*p.y*(1.-p.y)*4.;
    vig = pow(vig,0.3);
    col *= vig;
    gl_FragColor = vec4(col, 1.);
}
