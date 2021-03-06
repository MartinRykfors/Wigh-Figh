#version 120
uniform float time;
uniform vec2 size;
uniform float atime;
uniform float stat;
uniform float dit;
vec2 fragPosition;

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
    //f+= time*8.;// + a*4.;
    float qa = quantize(a, 8.);
    float qf = quantize(f, 8.);
    float g = abs(a-qa)*16.;
    float h = abs(f-qf)*16.;
    g = pow(g,20.);
    h = pow(h,10.);
    return (h+g) * vec3(0.3, 0.4, 0.9)*3.8;
}

vec3 horizon(vec3 dir){
    //float a = atan(dir.y, dir.x);
    float f = dir.z;
    vec3 nadir = vec3(.1,.12,.25);
    vec3 ground = vec3(.1,.11,.14);
    vec3 sky = vec3(.5);
    vec3 zenith = vec3(.0, .0, .1);
    vec3 col = f < 0. ? mix(nadir, ground, f+1.) : mix(sky, zenith, pow(f,.25));
    return col;// * (5.+sin(a*2.))/6.*2.5;
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

vec3 grid(vec3 dir, bool vert){
    vec2 p = dir.xy / max(0.001, abs(dir.z));
    p *= 3.;
    p.y *= 0.06;
    p.y += time * 20.3;
    vert = hash(floor(p.y/5. + 0.5)) < 0.5 ? vert : !vert;
    p += 0.5;
    float h = hash(floor(p*sign(dir.z)));
    float h2 = hash(floor(p.y/6.));
    float h3 = hash(floor(p.y/20.)+sign(dir.z));
    float band = abs(p.x) < 2. + floor(30.*h3*h3) ? 1. : 0.;
    p = mod(p, vec2(1.));
    p -= 0.5;
    float f = h2 < 0.5 ? smoothstep(0.6, 0.0,length(p))*9. : 1.;
    h = h < h2/1.2 + 0.1 && vert ? 1. : 0.;
    vec3 acc = hsv2rgb(vec3(h2/0.3-.3,.4,0.9))*h*band*3.*f;

    return acc*pow(abs(dir.z),.5);
}

float onoff(float a, float h){
    float f = sin(a*2. +a * floor(8.*h) + a*.5*h*(sin(a * 10.)));
    f = f < -0.5 ? 1. : 0.;
    return f;
}

vec3 circles(vec3 dir){
    vec2 p = dir.xy / max(0.001, acos(-abs(dir.z))/4.);
    float rr = length(p) * 10.;
    float a = atan(p.x, p.y);
    float r = mod(rr, 2.);
    float h = hash(rr-r + sign(dir.z));
    float t = time + (rr-r)/30. + floor(h * 9.);
    float s = hash(floor(t+h)) * 10.; // adding h to t here creates discontinuities
    float e = hash(floor(t+h) + 1.) * 10.;
    t = fract(t);
    t = clamp(t*3.2, 0., 1.);
    // t = sqrt(t);
    // t *= t;
    // t = pow(t, h*2.);
    t = t*e + (1.-t)*s;
    a += 3.1415;
    a += t;
    a = mod(a, 3.1415*2.);
    float cut = rr < 24. && rr > 8. ? 1. : 0.;
    float m = 0.1;
    float f = smoothstep(.0, .0+m, r)
        *(1.-smoothstep(1.5, 1.5+m, r))
        *onoff(a, h);
    return f*hsv2rgb(vec3(.0, .9, 1.)) * 3.5 * cut;
}

vec3 background(vec3 dir){
    // return spheregrid(dir);
    if (stat == 0.){
        vec3 g = grid(dir.zxy, true)
            + grid(dir.yxz, false);
        return g;
        // vec3 c = circles(dir.zyx);
        // return clamp(c + (1.-c*2.2)*g, 0., 5.);
    }
    else if (stat == 1.){
        float f = (1.5 + fract(fragPosition.y*3. + time*5.)/2.)/2.;
        return pow(hash(dir), 3.) * vec3(4.) * f;
    }
    else{
        return horizon(dir);
    }
    //return grid(dir) + grid(dir.yzx) + grid(dir.zxy);
    //return hash(dir) * vec3(1.);
    //return grid(dir*rotation) + grid(dir.yzx*rotation) + grid(dir.zxy*rotation);
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

vec3 glowColor = vec3(1.9, 1.4, 2.2);

vec3 render(Ray ray){
    float dist = 0.;
    vec3 pos;
    float minDist = 1000.;
    float curMap;
    for (int i = 0; i < 50; i++){
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
        rf *= rf;
        flash = sqrt(flash);
        return diffuse*(1.-rf)*0.4 + flash*f*glowColor*2.5 + refl*rf*1.3; 
    }
    float glow = 0.1/minDist;

    flash *=flash;
    return background(ray.dir)*0.9 + glow * glowColor * flash;
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

uniform float bayer [64] = float[64](1.,49.,13.,61.,4.,52.,16.,64.,33.,17.,45.,29.,36.,20.,48.,32.,9.,57.,5.,53.,12.,60.,8.,56.,41.,25.,37.,21.,44.,28.,40.,24.,3.,51.,15.,63.,2.,50.,14.,62.,35.,19.,47.,31.,34.,18.,46.,30.,11.,59.,7.,55.,10.,58.,6.,54.,43.,27.,39.,23.,42.,26.,38.,22);

vec2 quantize(vec2 p, float q){
    return floor(p*q)/q;
}

float coarseness = 4.;

vec3 dither(vec3 col){
    vec2 frag = gl_FragCoord.xy/coarseness;
    int i = int(mod(frag.x,8.));
    int j = int(mod(frag.y,8.));
    int index = i + 8*j;
    float b = bayer[index]/65.;
    b = pow(b,1.8);
    return step(b,col);
}

void main(){
    vec2 p = gl_FragCoord.xy / size;
    if (dit == 1.0){
        float jitterY = sin(time*10.) < 0 ? 1. : 0.;
        float jitterX = sin(time*30.) < 0.8 ? 1. : 0.;
        p.y += sin(time*(100. + 40.*sin(time)))*0.04*jitterY + 0.2*jitterY;
        p.y = mod(p.y, 1.);
        p.x += sin(p.y*7.)*0.08 * sin(time*100.)*jitterX;
        p.x = mod(p.x, 1.);
        p.x = quantize(p.x,size.x/coarseness/2.);
        p.y = quantize(p.y,size.y/coarseness/2.);
    }
    fragPosition = p;
	vec3 cameraPos = vec3(-8.,0.,-4.);
	// vec3 cameraPos = vec3(6.*sin(-3./3.),6.*cos(3./3.),-4.*sin(time/8.));
	vec3 lookAt = vec3(0.);
	vec3 up = vec3(0.,0.,1.);
	float aspect = size.x/size.y;
    float t = floor(time);
    //stat = fract(time/4.5) < 0.8 ? 0. : 1.;
    float vig = p.x*(1.-p.x)*p.y*(1.-p.y)*4.;
    vig = pow(vig,0.3);
    if (stat == 1.){
        p.x += (pow(hash(quantize(p.y+hash(time), 128.)), 8.)+0.1) * (hash(time/10.)-.5)*.3;
    }
    // float f = fract(time);
    // t += 1. - exp(-f*9.);
    // atime = t;
    rotation = rotateX(atime*0.3)*rotateY(atime*0.7);
    jitter = sin(time*100.)*.5*(1.-fract(atime));
	Ray ray = createRay(cameraPos, lookAt, up, p, 90., aspect);
    vec3 col = render(ray);
    col *= vig;
    // p -= .5;
    // col = circles(vec3(p*5.,1.));
    //col = grid(vec3(p*50.,1.),true);
    if (dit == 1.0){
        col = dither(col);
    }
    gl_FragColor = vec4(col, 1.);
}
