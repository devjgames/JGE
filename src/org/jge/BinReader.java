package org.jge;

public class BinReader {
    
    private final byte[] bytes;
    private int i = 0;

    public BinReader(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getLength() {
        return bytes.length;
    }

    public int getPosition() {
        return i;
    }

    public void setPosition(int i) {
        this.i = i;
    }

    public int readByte() {
        return ((int)bytes[i++]) & 0xFF;
    }

    public int readShort() {
        int b1 = ((int)bytes[i++]) & 0xFF;
        int b2 = ((int)bytes[i++]) & 0xFF;
       
        return ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public int readInt() {
        int b1 = ((int)bytes[i++]) & 0xFF;
        int b2 = ((int)bytes[i++]) & 0xFF;
        int b3 = ((int)bytes[i++]) & 0xFF;
        int b4 = ((int)bytes[i++]) & 0xFF;
       
        return ((b4 << 24) & 0xFF000000) | ((b3 << 16) & 0xFF0000) | ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public String readString(int length) {
        int n = 0;

        for(int j = i; j != length; j++, n++) {
            if(bytes[j] == 0) {
                break;
            }
        }

        String s = new String(bytes, i, n);

        i += length;

        return s;
    }

    public void readBytes(byte[] bytes, int i, int length) {
        for(int j = 0; j != length; j++, this.i++, i++) {
            bytes[i] = this.bytes[this.i];
        }
    }

    public void readBytes(byte[] bytes) {
        readBytes(bytes, 0, bytes.length);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];

        readBytes(bytes);

        return bytes;
    }

    public byte[] readToEnd() {
        return readBytes(getLength() - getPosition());
    }
}
