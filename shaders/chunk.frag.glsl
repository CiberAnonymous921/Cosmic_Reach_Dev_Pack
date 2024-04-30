#version 150
#ifdef GL_ES 
precision mediump float;
#endif

uniform vec3 skyAmbientColor;

in vec2 v_texCoord0;
in vec3 worldPos;
in vec4 blocklight;

uniform sampler2D texDiffuse;

out vec4 outColor;

void main() 
{
    vec2 tilingTexCoords = v_texCoord0;

    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if(texColor.a == 0)
    {
        discard;
    }

    // https://www.desmos.com/calculator
    // y\ =\ \frac{30}{1+e^{-15\left(\frac{x}{25}\right)^{2}}}-15
    vec3 it =  pow(15*blocklight.rgb / 25.0, vec3(2));
    vec3 t = 30.0/(1.0 + exp(-15.0 * it)) - 15;
    vec3 lightTint = max(t/15, blocklight.a * skyAmbientColor);


    outColor = vec4(texColor.rgb * lightTint, texColor.a);
}