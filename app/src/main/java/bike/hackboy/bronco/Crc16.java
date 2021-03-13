package bike.hackboy.bronco;

public class Crc16 {
    private static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;

    // I just copied this from decompiled code, enter at your own risk
    public static byte[] getChecksum(byte[] bArr) {
        int i = 0;
        int i2 = DEFAULT_INITIAL_WINDOW_SIZE;
        for (byte b : bArr) {
            i2 ^= b & 255;
            for (int i3 = 8; i3 > 0; i3--) {
                int i4 = i2 & 1;
                i2 >>= 1;
                if (i4 != 0) {
                    i2 ^= 40961;
                }
            }
        }
        int[] iArr = {i2 & 255, (i2 >> 8) & 255};
        byte[] bArr2 = new byte[2];
        int i5 = 0;
        while (i < 2) {
            bArr2[i5] = (byte) iArr[i];
            i++;
            i5++;
        }
        return bArr2;
    }
}
