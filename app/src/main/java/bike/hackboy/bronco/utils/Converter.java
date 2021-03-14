package bike.hackboy.bronco.utils;

import com.google.protobuf.UnknownFieldSet;

public class Converter {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static short fieldToShort(UnknownFieldSet.Field field) {
        return field
            .toByteString(0)
            .asReadOnlyByteBuffer()
            .getShort(0);
    }

    public static int fieldToInt(UnknownFieldSet.Field field) {
        return field
            .toByteString(0)
            .asReadOnlyByteBuffer()
            .getInt(0);
    }
}
