#version 120
uniform float time;
uniform vec2 size;

uniform float bayer [64] = float[64](1.,49.,13.,61.,4.,52.,16.,64.,33.,17.,45.,29.,36.,20.,48.,32.,9.,57.,5.,53.,12.,60.,8.,56.,41.,25.,37.,21.,44.,28.,40.,24.,3.,51.,15.,63.,2.,50.,14.,62.,35.,19.,47.,31.,34.,18.,46.,30.,11.,59.,7.,55.,10.,58.,6.,54.,43.,27.,39.,23.,42.,26.,38.,22);

vec2 quantize(vec2 p, float q){
    return floor(p*q)/q;
}

vec3 dither(vec3 col){
    vec2 frag = gl_FragCoord.xy/2.;
    int i = int(mod(frag.x,8.));
    int j = int(mod(frag.y,8.));
    int index = i + 8*j;
    float b = bayer[index]/65.;
    return step(b,col);
}

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
  for( int i = 0; i < 3; i++) {
    mat2 modify = rotate(time/50. * float(i*i));
    f += amp*noise(p);
    p = modify * p;
    p *= 2.;
    amp /= 2.2;
  }
  return f;
}

float pattern(vec2 p, out vec2 q, out vec2 r) {
  q = vec2( fbm(p + vec2(1.)), fbm(rotate(.1*time)*p + vec2(1.)));
  r = vec2( fbm(rotate(.1)*q + vec2(0.)), fbm(q + vec2(0.)));
  return fbm(p + 1.*r);
}

void main() {
    vec2 uv = gl_FragCoord.xy / size;
    uv = quantize(uv, 600./2.);
    vec2 q;
    vec2 r;
    float b = pattern(uv,q,r);
    vec3 col = vec3(b,q.x,r.y);
    col *= 1.9;
    col -= 0.25;
    col = dither(col);
    gl_FragColor = vec4(vec3(col.z),1.);
}
