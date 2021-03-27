package bike.hackboy.bronco.data;

import java.util.Arrays;

import bike.hackboy.bronco.utils.Crc16;

public class Command {
	public static final byte[] SET_SPEED = {10, 16, 0, 4, 0, 1, 2, 0, 30};
	public static final byte[] READ_SPEED = {10, 3, 0, 4, 0, 1};

	public static final byte[] SET_FIELD_WEAKENING = {1, 16, 0, (byte) 129, 0, 1, 2, 0, 0};
	public static final byte[] READ_FIELD_WEAKENING = {1, 3, 0, (byte) 129, 0, 1};

	public static final byte[] READ_MOTOR_MODE = {1, 3, 0, 11, 0, 1};
	public static final byte[] SET_MOTOR_MODE_TORQUE = {1, 16, 0, 11, 0, 1, 2, 0, 1};
	public static final byte[] SET_MOTOR_MODE_TORQUE_WITH_LIMIT = {1, 16, 0, 11, 0, 1, 2, 0, 2};

	public static final byte[] WRITE_FLASH = {1, 16, 1, (byte) 255, 0, 1, 2, 127, (byte) 255};
	public static final byte[] CLOSE_FLASH = {1, 16, 1, (byte) 255, 0, 1, 2, 0, 0};

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
		byte[] changedCommandWithChecksum = Arrays.copyOf(command, command.length + 2);

		changedCommandWithChecksum[command.length] = checksum[0];
		changedCommandWithChecksum[command.length + 1] = checksum[1];

		return changedCommandWithChecksum;
	}
}
