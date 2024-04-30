package finalforeach.cosmicreach.rendering.shaders;

import java.util.HashSet;

import org.lwjgl.opengl.GL32;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.TextureBuffer;

public class GameShader {
    protected static final long startTime = System.currentTimeMillis();
    private static Array<GameShader> allShaders = new Array<GameShader>();
    private String vertexShaderFileName;
    private String fragShaderFileName;
    public ShaderProgram shader;
    public VertexAttribute[] allVertexAttributes;
    public static String macOSPrependVert = "#define attribute in\n#define varying out\n#define MACOS 1\n";
    public static String macOSPrependFrag = "#define varying in\nout vec4 fragColor;\n#define textureCube texture\n#define texture2D texture\n#define gl_FragColor fragColor\n#define MACOS 1\n";
    public static String macOSPrependVertVer = "#version 150\n" + macOSPrependVert;
    public static String macOSPrependFragVer = "#version 150\n" + macOSPrependFrag;
    private float[] tmpVec3 = new float[3];

    public static void initShaders() {
        ChunkShader.initChunkShaders();
        SkyStarShader.initSkyStarShader();
    }

    public static void reloadAllShaders() {
        System.out.println("Reloading all shaders...");
        for (GameShader chunkShader : allShaders) {
            chunkShader.reload();
        }
        System.out.println("Reloaded all shaders.");
    }

    @SuppressWarnings("incomplete-switch")
	private String loadShaderFile(String shaderName, ShaderType shaderType) {
        String[] rawShaderLines = GameAssetLoader.loadAsset("shaders/" + shaderName).readString().split("\n");
        StringBuilder sb = new StringBuilder();
        Object version = "";
        String define = shaderName.replaceAll("[-/. ()]", "_");
        sb.append("#ifndef " + define + "\n");
        sb.append("#define " + define + "\n");
        for (String shaderLine : rawShaderLines) {
            if (shaderLine.startsWith("#version")) {
                version = shaderLine + "\n";
                if (!RuntimeInfo.isMac) continue;
                switch (shaderType) {
                    case FRAG: {
                        version = (String)version + macOSPrependFrag;
                        break;
                    }
                    case VERT: {
                        version = (String)version + macOSPrependVert;
                        break;
                    }
                }
                continue;
            }
            if (shaderLine.startsWith("#import \"") && shaderLine.endsWith("\"")) {
                String importedShaderName = shaderLine.replaceFirst("#import \"", "").replace("\\", "/");
                importedShaderName = importedShaderName.substring(0, importedShaderName.length() - 1);
                sb.append(this.loadShaderFile(importedShaderName, ShaderType.IMPORTED) + "\n");
                continue;
            }
            sb.append(shaderLine + "\n");
        }
        sb.append("#endif //" + define + "\n");
        return (String)version + sb.toString();
    }

    public String preProcessShaderFile(String shaderText, ShaderType shaderType) {
        return shaderText;
    }

    public void validateShader(String vertFileName, String vertShaderText, String fragFileName, String fragShaderText) {
        String[] vertLines = vertShaderText.split(";");
        String[] fragLines = fragShaderText.split(";");
        HashSet<String> vertOuts = new HashSet<String>();
        for (String vLine : vertLines) {
            String v = vLine.trim();
            if (!v.startsWith("out ")) continue;
            String out = v.replace("out ", "");
            if (vertOuts.contains(out)) {
                throw new RuntimeException("Vert shader " + vertFileName + ": Cannot declare out param '" + out + "' twice.");
            }
            vertOuts.add(out);
        }
        HashSet<String> missingFragIns = new HashSet<String>(vertOuts);
        HashSet<String> fragIns = new HashSet<String>();
        for (String fLine : fragLines) {
            String f = fLine.trim();
            if (!f.startsWith("in ")) continue;
            String in = f.replace("in ", "");
            if (!vertOuts.contains(in)) {
                throw new RuntimeException("Vert shader " + vertFileName + ": Missing out param '" + in + "' to pass to frag shader.");
            }
            if (fragIns.contains(in)) {
                throw new RuntimeException("Frag shader " + fragFileName + ": Cannot declare in param '" + in + "' twice.");
            }
            fragIns.add(in);
            missingFragIns.remove(in);
        }
        if (missingFragIns.size() > 0) {
            Object missingInStr = "";
            for (Object i : missingFragIns) {
                missingInStr = (String)missingInStr + i + ", ";
            }
            throw new RuntimeException("Frag shader " + fragFileName + ": Missing in params from vert shader: " + (String)missingInStr);
        }
    }

    public void reload() {
        if (this.shader != null) {
            this.shader.dispose();
        }
        if (RuntimeInfo.isMac) {
            ShaderProgram.prependVertexCode = "";
            ShaderProgram.prependFragmentCode = "";
        }
        String vert = this.preProcessShaderFile(this.loadShaderFile(this.vertexShaderFileName, ShaderType.VERT), ShaderType.VERT);
        String frag = this.preProcessShaderFile(this.loadShaderFile(this.fragShaderFileName, ShaderType.FRAG), ShaderType.FRAG);
        this.validateShader(this.vertexShaderFileName, vert, this.fragShaderFileName, frag);
        ShaderProgram.pedantic = true;
        this.shader = new ShaderProgram(vert, frag);
        System.out.println("Compiling shader(" + this.vertexShaderFileName + ", " + this.fragShaderFileName + ")...");
        if (!this.shader.isCompiled()) {
            String log = this.shader.getLog();
            throw new RuntimeException(this.getClass().getSimpleName() + " is not compiled!\n" + log);
        }
        for (String u : this.shader.getUniforms()) {
            if (u.contains(".")) {
                int blockIndex = GL32.glGetUniformBlockIndex(this.shader.getHandle(), u.split("\\.")[0]);
                System.out.println("Loaded uniform: " + this.getUniformTypeName(u) + " " + u + " at location=" + blockIndex);
                continue;
            }
            System.out.println("Loaded uniform: " + this.getUniformTypeName(u) + " " + u + " at location=" + this.shader.getUniformLocation(u));
        }
        System.out.println(this.shader.getLog());
        if (RuntimeInfo.isMac) {
            ShaderProgram.prependVertexCode = macOSPrependVertVer;
            ShaderProgram.prependFragmentCode = macOSPrependFragVer;
        }
    }

    public GameShader(String vertexShader, String fragmentShader) {
        this.vertexShaderFileName = vertexShader;
        this.fragShaderFileName = fragmentShader;
        allShaders.add(this);
        this.reload();
    }

    public int getUniformLocation(String uniformName) {
        return this.shader.getUniformLocation(uniformName);
    }

    public String getUniformTypeName(String uniformName) {
        int uType = this.shader.getUniformType(uniformName);
        switch (uType) {
            case 5124: {
                return "GL_INT";
            }
            case 5125: {
                return "GL_UNSIGNED_INT";
            }
            case 5126: {
                return "GL_FLOAT";
            }
            case 35664: {
                return "GL_FLOAT_VEC2";
            }
            case 35665: {
                return "GL_FLOAT_VEC3";
            }
            case 35666: {
                return "GL_FLOAT_VEC4";
            }
            case 35674: {
                return "GL_FLOAT_MAT2";
            }
            case 35675: {
                return "GL_FLOAT_MAT3";
            }
            case 35676: {
                return "GL_FLOAT_MAT4";
            }
            case 35678: {
                return "GL_SAMPLER_2D";
            }
            case 36290: {
                return "GL_SAMPLER_BUFFER";
            }
        }
        return "Unknown uniform type[" + uType + "]";
    }

    public void bindOptionalFloat(String uniformName, float value) {
        int u = this.getUniformLocation(uniformName);
        if (u != -1) {
            this.shader.setUniformf(u, value);
        }
    }

    public void bindOptionalInt(String uniformName, int value) {
        int u = this.getUniformLocation(uniformName);
        if (u != -1) {
            this.shader.setUniformi(u, value);
        }
    }

    public int bindOptionalTextureBuffer(String uniformName, TextureBuffer texBuf, int texNum) {
        int u = this.getUniformLocation(uniformName);
        if (u != -1) {
            texBuf.bind(texNum);
            this.shader.setUniformi(u, texNum);
            return texNum + 1;
        }
        return texNum;
    }

    public int bindOptionalTexture(String uniformName, Texture tex, int texNum) {
        int u = this.getUniformLocation(uniformName);
        if (u != -1) {
            tex.bind(texNum);
            this.shader.setUniformi(u, texNum);
            return texNum + 1;
        }
        return texNum;
    }

    public void bindOptionalUniform3f(String uniformName, Vector3 vec3) {
        this.bindOptionalUniform3f(uniformName, vec3.x, vec3.y, vec3.z);
    }

    public void bindOptionalUniform3f(String uniformName, Color color) {
        this.bindOptionalUniform3f(uniformName, color.r, color.g, color.b);
    }

    public void bindOptionalUniform3f(String uniformName, float x, float y, float z) {
        int u = this.getUniformLocation(uniformName);
        if (u != -1) {
            this.tmpVec3[0] = x;
            this.tmpVec3[1] = y;
            this.tmpVec3[2] = z;
            this.shader.setUniform3fv(u, this.tmpVec3, 0, 3);
        }
    }

    public void bind(Camera worldCamera) {
        this.shader.bind();
        this.bindOptionalFloat("u_time", (float)(System.currentTimeMillis() - startTime) / 1000.0f);
        this.bindOptionalUniform3f("cameraPosition", worldCamera.position);
    }

    public void unbind() {
    }

    static enum ShaderType {
        FRAG,
        VERT,
        IMPORTED;

    }
}