package finalforeach.cosmicreach.savelib;

public interface IChunkByteWriter {
    public <T> void writeBlockValue(T var1);

    public void writeInt(int var1);

    public void writeByte(int var1);

    public void writeBytes(byte[] var1);

    public void writeShorts(short[] var1);
}