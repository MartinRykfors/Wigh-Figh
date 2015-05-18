uniform float time;
uniform vec2 size;
float atime;

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
float jitter;

float hash(float f)
{
    return fract(sin(f*32.34182) * 43758.5453);
}

float hash(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float hash(vec3 co)
{
    return fract(sin(dot(co,vec3(12.9898,78.233, 34.3182))) * 43758.5453);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float quantize(float x, float q){
    x += .5/q;
    return x - fract(x*q)/q;
}

vec3 spheregrid(vec3 dir){
    float a = atan(dir.y, dir.x)/3.1415 ;
    float f = asin(dir.z) / 3.1415 ;
    f+= time*8.;// + a*4.;
    float qa = quantize(a, 8.);
    float qf = quantize(f, 8.);
    float g = abs(a-qa)*16.;
    float h = abs(f-qf)*16.;
    g = pow(g,20.);
    h = pow(h,10.);
    return (h+g) * vec3(0.3, 0.4, 0.9)*3.8;
}

vec3 horizon(vec3 dir){
    float a = atan(dir.y, dir.x);
    float f = dir.z;
    vec3 nadir = vec3(.1,.3,.5);
    vec3 ground = vec3(.1,.6,.2);
    vec3 sky = vec3(1.);
    vec3 zenith = vec3(.0, .0, .2);
    vec3 col = f < 0. ? mix(nadir, ground, f+1.) : mix(sky, zenith, pow(f,.25));
    return col * (5.+sin(a*2.))/6.*2.5;
}

float squares(vec2 p, float r){
    float rx = 6.;
    p.y += r/2.;
    p.y = mod(p.y, r);
    p.y -= r/2.;
    p.x *= 3.;
    p.x += rx/2.;
    p.x = mod(p.x, rx);
    p.x -= rx/2.;
    float m = 0.007;
    float ox = 0.5;
    float oy = 0.9;
    float fx = 0.02;
    float fy = 0.01;
    float f = smoothstep(0., .0+m, p.x)
        * (1.-smoothstep(ox, ox+m, p.x))
        * smoothstep(0., .0+m, p.y)
        * (1.-smoothstep(oy, oy+m, p.y));
    float i = smoothstep(fx, fx+m, p.x)
        * (1.-smoothstep(ox-fx, ox-fx+m, p.x))
        * smoothstep(fy, fy+m, p.y)
        * (1.-smoothstep(oy-fy, oy-fy+m, p.y));
    return f - i*.8;
}

vec3 grid(vec3 dir){
    vec2 p = dir.xy / max(0.001, abs(dir.z));
    vec3 acc = vec3(0.);
    float h = .5;
    //acc += max(0., 1.-abs(p.x - hash(quantize(p.y + time*10., 4.)))*38.)*hsv2rgb(vec3(h,0.8,1.))*3.;
    for (int i = 0; i<10; i++){
        p.x += sin(float(i)*2. + time/8.)*1.;
        p.y += 0.1*float(i+4)*time;
        p*=1.05;
        h+=0.01;
        acc += squares(p, 3.)* hsv2rgb(vec3(h, 0.9, 0.9)) * 3.3;
    }
    return vec3(acc)*pow(abs(dir.z),.4);
}

vec3 background(vec3 dir){
    //return spheregrid(dir);
    //return horizon(dir);
    return grid(dir) + grid(dir.yzx) + grid(dir.zxy);
    // return grid(dir*rotation) + grid(dir.yzx*rotation) + grid(dir.zxy*rotation);
}

vec4 box(vec3 p, float w){
    p = abs(p);
    float dx = p.x-w;
    float dy = p.y-w;
    float dz = p.z-w;
    float m = max(p.x-w, max(p.y-w, p.z-w));
    return vec4(m,dx,dy,dz);
}

vec4 map(vec3 p){
    float e = 0.2;
    for (int i = 0; i < 5; i++){
        p = abs(p*rotation + vec3(0.1, .0, .0));
        p.y -= .8;
        p.x -= .06;
        p.z -= jitter;
        p.xy = p.yx;
    }
    return box(p, .6);
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

vec3 glowColor = vec3(2.9, 1.4, 1.2);

vec3 render(Ray ray){
    float dist = 0.;
    vec3 pos;
    float minDist = 1000.;
    float curMap;
    for (int i = 0; i < 60; i++){
        pos = ray.org + dist*ray.dir;
        curMap = map(pos).x;
        dist+=curMap;
        minDist = min(minDist,curMap);
    }
    vec4 m = map(pos);
    float flash = 1.-fract(atime);
    if (m.x < 0.01){
        vec3 n = normal(pos);
        vec3 l = normalize(vec3(1.,2.,5.));
        vec3 diffuse = clamp(dot(n, l),0., 1.)*vec3(1.);
        vec3 r = reflect(ray.dir, n);
        vec3 refl = background(r);
        float dx = m.y;
        float dy = m.z;
        float dz = m.w;
        float start = 0.00;
        float end = 0.09*flash + 0.02;
        float f = smoothstep(start, end, abs(dx-dy));
        f *= smoothstep(start, end, abs(dx-dz));
        f *= smoothstep(start, end, abs(dz-dy));
        f = 1. - f;
        float rf = 1.-abs(dot(ray.dir, n));
        rf = pow(rf,3.);
        flash = sqrt(flash);
        return diffuse*(1.-rf)*0.4 + flash*f*glowColor*2.5 + refl*rf*1.3; 
    }
    float glow = 0.1/minDist;

    flash *=flash;
    return background(ray.dir)*0.6 + glow * glowColor * flash;
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
	//vec3 cameraPos = vec3(6.*sin(time/3.),6.*cos(time/3.),-4.*sin(time/8.));
	//vec3 cameraPos = vec3(5.*sin(time/3.),5.*cos(time/3.),-8);
	vec3 cameraPos = vec3(0.,5.,-5.);
	vec3 lookAt = vec3(0.);
	vec3 up = vec3(0.,0.,1.);
	float aspect = size.x/size.y;
    float t = floor(time);
    float f = fract(time);
    t += 1. - exp(-f*9.);
    atime = t;
    rotation = rotateX(atime*1.9)*rotateY(atime*1.4);
    jitter = sin(time*80.)*.1*pow((1.-fract(time)),5.);
	Ray ray = createRay(cameraPos, lookAt, up, p, 90., aspect);
    vec3 col = render(ray);
    float vig = p.x*(1.-p.x)*p.y*(1.-p.y)*4.;
    vig = pow(vig,0.3);
    col *= vig;
    //col = grid(vec3(p*4.,1.));
    gl_FragColor = vec4(col, 1.);
}
