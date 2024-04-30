package finalforeach.cosmicreach.items;

import com.badlogic.gdx.graphics.Camera;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class ItemBlock extends Item {
    public final BlockState blockState;
    IGameMesh mesh;
    GameShader shader;

    public ItemBlock(BlockState blockState) {
        this.blockState = blockState;
        MeshData meshData = blockState.isTransparent ? (blockState.getBlock() == Block.WATER ? new MeshData(ChunkShader.WATER_BLOCK_SHADER, RenderOrder.TRANSPARENT) : new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.TRANSPARENT)) : new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.DEFAULT);
        this.shader = meshData.getShader();
        blockState.addVertices(meshData, 0, 0, 0);
        if (BlockModelJson.useIndices) {
            this.mesh = meshData.toIntIndexedMesh(true);
        } else {
            this.mesh = meshData.toSharedIndexMesh(true);
            if (this.mesh != null) {
                int numIndices = this.mesh.getNumVertices() * 6 / 4;
                SharedQuadIndexData.allowForNumIndices(numIndices, false);
            }
        }
    }

    @Override
    public void render(Camera camera) {
        if (this.mesh != null) {
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }
            this.shader.bind(camera);
            this.shader.shader.setUniformMatrix("u_projViewTrans", camera.combined);
            this.mesh.bind(this.shader.shader);
            this.mesh.render(this.shader.shader, 4);
            this.mesh.unbind(this.shader.shader);
            this.shader.unbind();
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.unbind();
            }
        }
    }
}