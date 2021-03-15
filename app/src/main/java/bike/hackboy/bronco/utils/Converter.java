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

	// @ me if you're still using this app in 2037
	public static int[] secondsToTime(int time) {
		int hours = time / 3600;
		int remainder = time - hours * 3600;
		int minutes = remainder / 60;
		remainder = remainder - minutes * 60;
		int secs = remainder;

		return new int[]{hours, minutes, secs};
	}
}
