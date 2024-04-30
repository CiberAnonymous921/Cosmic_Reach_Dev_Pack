#ifdef MACOS

in vec2 a_uv;
#define GET_TEX_COORDS a_uv

#else

in int a_uvIdx;
#define GET_TEX_COORDS getTexCoordsFromUVIdx(a_uvIdx)

uniform samplerBuffer texBuffer;

vec2 getTexCoords(int uvAoBits)
{
    int texId = 2*((uvAoBits >> 2) & 16383);
    return vec2(texelFetch(texBuffer, texId).r, texelFetch(texBuffer, texId+1).r);
}

vec2 getTexCoordsFromUVIdx(int texId)
{
    return vec2(texelFetch(texBuffer, 2*texId).r, texelFetch(texBuffer, (2*texId)+1).r);
}

#endif

int getUvAoBits(int uvAoPackedColor)
{
    return (uvAoPackedColor >> 16) & 65535;
}

int getPackedColorBits(int uvAoPackedColor)
{
    return uvAoPackedColor & 65535;
}

vec4 getBlockLight(int packedColorBits)
{
    return vec4(((packedColorBits) & 15), (packedColorBits >> 4) & 15, (packedColorBits >> 8) & 15, (packedColorBits >> 12)) / 16.0;
}

float getAO(int uvAoBits)
{
    return (((uvAoBits & 3)) / 4.0) + 0.25;
}