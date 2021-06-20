package bike.hackboy.bronco.utils;

import android.location.Location;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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

	public static String rot13(String input) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			if       (c >= 'a' && c <= 'm') c += 13;
			else if  (c >= 'A' && c <= 'M') c += 13;
			else if  (c >= 'n' && c <= 'z') c -= 13;
			else if  (c >= 'N' && c <= 'Z') c -= 13;

			sb.append(c);
		}
		return sb.toString();
	}

	public static String floatCoordsToDegrees(double coords) {
		StringBuilder builder = new StringBuilder();

		String latitudeDegrees = Location.convert(Math.abs(coords), Location.FORMAT_SECONDS);
		String[] latitudeSplit = latitudeDegrees.split(":");

		builder.append(latitudeSplit[0]);
		builder.append("°");
		builder.append(latitudeSplit[1]);
		builder.append("′");
		builder.append(latitudeSplit[2].replace(',', '.'));
		builder.append("″");

		return builder.toString();
	}

	public static String formatDecimal(float number) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat decimalFormat = new DecimalFormat("#.00", symbols);
		decimalFormat.setRoundingMode(RoundingMode.CEILING);
		return decimalFormat.format(number);
	}

	public static String concatList(Iterable<String> strings, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(String s: strings) {
			sb.append(sep).append(s);
			sep = separator;
		}
		return sb.toString();
	}
}
