#version 120
uniform vec2 size;
uniform float time;

uniform float font[250] = float[250](0.,1.,1.,1.,0.,1.,0.,0.,1.,1.,1.,0.,1.,0.,1.,1.,1.,0.,0.,1.,0.,1.,1.,1.,0.,0.,0.,1.,0.,0.,0.,1.,1.,0.,0.,0.,0.,1.,0.,0.,0.,0.,1.,0.,0.,0.,1.,1.,1.,0.,1.,1.,1.,1.,0.,0.,0.,0.,0.,1.,0.,1.,1.,1.,0.,1.,0.,0.,0.,0.,1.,1.,1.,1.,1.,1.,1.,1.,1.,0.,0.,0.,0.,0.,1.,0.,0.,1.,1.,0.,0.,0.,0.,0.,1.,1.,1.,1.,1.,0.,1.,0.,0.,0.,1.,1.,0.,0.,0.,1.,0.,1.,1.,1.,1.,0.,0.,0.,0.,1.,0.,0.,0.,0.,1.,1.,1.,1.,1.,1.,1.,0.,0.,0.,0.,1.,1.,1.,1.,0.,0.,0.,0.,0.,1.,1.,1.,1.,1.,0.,0.,1.,1.,1.,1.,1.,0.,0.,0.,0.,1.,1.,1.,1.,0.,1.,0.,0.,0.,1.,0.,1.,1.,1.,0.,1.,1.,1.,1.,1.,0.,0.,0.,0.,1.,0.,0.,0.,1.,0.,0.,0.,0.,1.,0.,0.,0.,0.,1.,0.,0.,1.,1.,1.,0.,1.,0.,0.,0.,1.,0.,1.,1.,1.,0.,1.,0.,0.,0.,1.,0.,1.,1.,1.,0.,0.,1.,1.,1.,0.,1.,0.,0.,0.,1.,0.,1.,1.,1.,1.,0.,0.,0.,0.,1.,0.,1.,1.,1.,0.);

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 color; 

struct Ray
{
	vec3 org;
	vec3 dir;
};

float noise(vec2 p)
{
  return sin(p.x*10.) * sin(p.y*(3. + sin(time/11.))) + .2; 
}

mat2 rotate(float angle)
{
  return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}


float fbm(vec2 p)
{
  p *= 1.1;
  float f = 0.;
  float amp = .5;
  for(int i = 0; i < 3; i++) {
      mat2 modify = rotate(time/(2700. + 400.*sin(time/80.)) * float(i*i));
    f += amp*noise(p);
    p = modify * p;
    p *= 2.;
    amp /= 1.5;
  }
  return f;
}

float pattern(vec2 p, out vec2 q, out vec2 r) {
  q = vec2( fbm(p + vec2(1.)), fbm(rotate(.01*time)*p + vec2(1.)));
  r = vec2( fbm(rotate(.1)*q + vec2(0.)), fbm(q + vec2(0.)));
  return fbm(p + 1.*r);

}

float digit(vec2 p){
    vec2 grid = vec2(3.,1.) * 15.;
    vec2 s = floor(p * grid) / grid;
    p = p * grid;
    vec2 q;
    vec2 r;
    float intensity = clamp((pattern(s/40., q, r) - 0.1 )*1.9, 0., 0.99) ;
    p = fract(p);
    p *= vec2(1.1, 1.2);
    float x = fract(p.x * 5.);
    float y = fract((1. - p.y) * 5.);
    int i = int(floor((1. - p.y) * 5.));
    int j = int(floor(p.x * 5.));
    int offset = int(floor(intensity * 10.)) * 25;
    return p.x <= 1. && p.y <= 1? font[i*5 + j + offset ] * (0.2 + y*4./5.) * (0.75 + x/4.) : 0.;
}

float hash(float x){
    return fract(sin(x*234.1)* 324.19 + sin(sin(x*3214.09) * 34.132 * x) + x * 234.12);
}

float onOff(float a, float b, float c)
{
	return step(c, sin(time + a*cos(time*b)));
}

float displace(vec2 look)
{
    float y = mod(look.y+time,2.)-1.0;
    float window = 2./(1.+220.*y*y);
	return sin(look.y*20. + time)/80.*onOff(4.,2.,.3)*(1.+cos(time*60.))*window;
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

vec3 getColor(vec2 p){
    float bar = 1.;//mod(p.y + time*10., 1.) < 0.3 ?  1.1 : 1.;
    //p.y*=1.5;
    p.x/=1.5;
    p.x += displace(p);
    float middle = digit(p);
    float off = 0.005;
    float sum = 0.;
            sum += digit(p);
    return  sum/1.*color* bar;
}

vec2 trajectory(float t){
    return vec2(sin(t/50.), cos(t/20.))*10.;
}

vec3 background(vec3 dir){
    float z = abs(dir.z);
    z = 1.-z;
    return color*pow(z,6.)*2.0;
}

vec3 render(vec2 p, float lTime){
    vec2 pos = vec2(sin(lTime/3.),cos(lTime/5.)) * 1.;
    vec3 up = vec3(0.,-1.,0.);
    Ray ray = createRay(vec3(trajectory(lTime),1.5+sin(lTime/10.)*1.1), vec3(trajectory(lTime-1.5),0.), up, p, 90, 2.);
    vec3 planePos = ray.org + ray.org.z*ray.dir/ray.dir.z;
    float intensity = clamp(-ray.dir.z,0.,1.);
    vec3 col = getColor(planePos.xy)*intensity;
    col *= 1.5;
    col+=background(ray.dir);
    col+=color*0.1;
    return col;
}

void main() {
    color = hsv2rgb(vec3(0.3, 0.5, 1.))*1.3;
    vec2 p = gl_FragCoord.xy / size.xy;
    float timeStep = 0.01;
    float amp = 1.;
    vec3 col = vec3(0);
    float sum = 0.;
    for (int i = 0; i < 4; i++){
        col += render(p, time-i*timeStep)*amp;
        sum+=amp;
        amp/=1.9;
    }
    col /= sum;
    // vec3 col = render(p, time);
    gl_FragColor = vec4(col,1);
}
