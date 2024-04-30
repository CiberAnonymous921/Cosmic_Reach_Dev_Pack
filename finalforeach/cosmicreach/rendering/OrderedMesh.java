package finalforeach.cosmicreach.rendering;

import finalforeach.cosmicreach.rendering.meshes.IntIndexedMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Chunk;

public record OrderedMesh(Chunk chunk, IntIndexedMesh mesh, GameShader shader, int renderOrder) {
}