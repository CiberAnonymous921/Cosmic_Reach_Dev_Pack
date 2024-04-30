#version 150
#ifdef GL_ES 
precision mediump float;
#endif

in vec3 worldPos;

out vec4 outColor;
layout(shared) uniform TestUBO
{
    vec4 uboColor;
} testUbo;

void main() 
{
    outColor = testUbo.uboColor;//vec4(1);
}