#version 150
#ifdef GL_ES 
precision mediump float;
#endif

in vec3 worldPos;

out vec4 outColor;

void main() 
{
    outColor = vec4(1);
}