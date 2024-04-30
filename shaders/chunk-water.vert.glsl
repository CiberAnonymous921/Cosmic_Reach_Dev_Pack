#version 150

in vec3 a_position;
//in int a_uvAoPackedColorAttrib;
in vec4 a_lighting;

uniform mat4 u_projViewTrans;
uniform float u_time;
uniform vec3 cameraPosition;

out vec2 v_texCoord0;
out vec4 blocklight;
out float waveStrength;
out vec3 worldPos;
out vec3 toCameraVector;

#import "common/bitUnpacker.glsl"

void main() 
{
    worldPos = a_position;

	v_texCoord0 = GET_TEX_COORDS;
	blocklight = a_lighting;

    toCameraVector = cameraPosition - worldPos;
    float waveSpeed = 0.25;
    float waveTime = waveSpeed * u_time;
    float scale = 5;
    float wavePositionA = 10 * (cos(worldPos.x*scale) + sin(worldPos.z*scale));
    float wavePositionB = 10 * (sin(worldPos.x*scale) + cos(worldPos.z*scale));

    waveStrength = 0.1 * (sin(waveTime + wavePositionA) * cos(waveTime + wavePositionB));
	
	gl_Position = u_projViewTrans * vec4(worldPos.x, worldPos.y + waveStrength - 0.2, worldPos.z, 1.0);
}
