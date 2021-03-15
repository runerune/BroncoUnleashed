package bike.hackboy.bronco.data;

import java.util.Arrays;

import bike.hackboy.bronco.utils.Crc16;

public class Command {
	public static final byte[] SET_SPEED = {10, 16, 0, 4, 0, 1, 2, 0, 30};
	public static final byte[] LOCK = {0};  //yes, seriously.
	public static final byte[] UNLOCK = {1};
	public static final byte[] LIGHT_ON = {10, 16, 0, 1, 0, 1, 2, 0, 1};
	public static final byte[] LIGHT_OFF = {10, 16, 0, 1, 0, 1, 2, 0, 0};

	public static byte[] withValue(byte[] template, int value) {
		byte[] changedCommand = Arrays.copyOf(template, 9);
		changedCommand[8] = (byte) value;

		return changedCommand;
	}

	public static byte[] withChecksum(byte[] command) {
		byte[] checksum = Crc16.getChecksum(command);
		byte[] changedCommandWithChecksum = Arrays.copyOf(command, 11);

		changedCommandWithChecksum[9] = checksum[0];
		changedCommandWithChecksum[10] = checksum[1];

		return changedCommandWithChecksum;
	}
}
