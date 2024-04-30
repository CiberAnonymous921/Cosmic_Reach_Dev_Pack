#version 150

in vec3 a_position;
//in int a_uvAoPackedColorAttrib;

in vec4 a_lighting;
uniform mat4 u_projViewTrans;

out vec2 v_texCoord0;
out vec3 worldPos;
out vec4 blocklight;

#import "common/bitUnpacker.glsl"

void main() 
{
	worldPos = a_position;
	
	//int packedColorBits = getPackedColorBits(a_uvAoPackedColorAttrib);
	blocklight = a_lighting;//getBlockLight(packedColorBits);
	
	//int uvAoBits = getUvAoBits(a_uvAoPackedColorAttrib);
	v_texCoord0 = GET_TEX_COORDS;

	gl_Position = (u_projViewTrans * vec4(worldPos, 1.0));
}
