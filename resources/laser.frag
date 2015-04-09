uniform vec2 size;
uniform float time;
uniform float animTime;

float rotationTime(){
    float tt = animTime;
    float part = floor(tt);
    float t = fract(tt);
    t = 1. - exp(-t * 10.);
    return part + t;
}

float oscillation(){
    float t = fract(animTime);
    return exp(-t * 3.);
}

vec2 mandelbox(vec2 z){
    float r = 0.9;
    for (float i = 0.; i < 4.; i += 1.){
        z = clamp(z,-1.,1.)*2. - z;
        if (length(z) < r){
            z /= r*r;
        }
        else if (length(z) < 1.){
            z /= length(z);
        }
        float a = 1.2 * rotationTime() * i;
        mat2 rot = mat2(cos(a), sin(a), -sin(a), cos(a));
        z *=rot;
        z = 1.3 * z + vec2(cos(rotationTime()) + 1., sin(rotationTime()) ) * 3.;
    }
    return z;
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float quantize(float x, float res){
    return x - fract(x/res)*res;
}

void main()
{
    vec2 p = gl_FragCoord.xy / size;
    p = p*2. - 1.;
    p.x += sin(rotationTime())/ 3.;
    float s = 18. + 9.*cos(rotationTime() * 3.);

    vec3 col1 = hsv2rgb(vec3(time/90., .8, .8));
    vec3 col2 = hsv2rgb(vec3(time/90. -.1, .8, .8));

    vec2 z = mandelbox(p*s) / 5.;
    float a = rotationTime() + time / 5.;
    mat2 rot = mat2(cos(a), sin(a), -sin(a), cos(a));
    float osc = oscillation();
    z *= rot;
    vec3 colx = col1 / abs(z.x + 0.4*sin(quantize(z.y, 0.3)*3.+animTime*100.) * osc) / 20.;
    z *= rot;
    vec3 coly = col2 / abs(z.y + 0.5*sin(quantize(z.x, 1.)*2.+animTime*100.) * osc) / 20.;
    vec3 colt = vec3(1.) / length(z) / (5. + 4.*sin(time*100.) * osc);
    vec2 uv = gl_FragCoord.xy / size;
    float vig = 1. - pow(4.*(uv.x - .5)*(uv.x - .5), 3.);
    vig *= 1. - pow(4.*(uv.y - .5)*(uv.y - .5), 3.);

    gl_FragColor = vec4((sqrt(colx) + colt + sqrt(coly))*vig,1.);
}
