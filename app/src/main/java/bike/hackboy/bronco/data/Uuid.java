package bike.hackboy.bronco.data;

import java.util.UUID;

public class Uuid {
	public static final String characteristicUnlockString = "C0B0A001-18EB-499D-B266-2F2910744274";
	public static final String characteristicDashboardString = "C0B0A00A-18EB-499D-B266-2F2910744274";
	public static final String characteristicSettingsReadString = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
	public static final String characteristicSettingsWriteString = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";

	public static final String characteristicDfcRequestString = "C0B0A005-18EB-499D-B266-2F2910744274";
	public static final String characteristicTripString = "C0B0A008-18EB-499D-B266-2F2910744274";

	public static final UUID notificationDescriptor = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

	public static final UUID serviceSettings = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	public static final UUID serviceCby = UUID.fromString("C0B0A000-18EB-499D-B266-2F2910744274");

	public static final UUID characteristicSettingsWrite = UUID.fromString(Uuid.characteristicSettingsWriteString);
	public static final UUID characteristicSettingsRead = UUID.fromString(Uuid.characteristicSettingsReadString);
	public static final UUID characteristicUnlock = UUID.fromString(Uuid.characteristicUnlockString);
	public static final UUID characteristicDashboard = UUID.fromString(Uuid.characteristicDashboardString);

	public static final UUID characteristicDfcRequest = UUID.fromString(Uuid.characteristicDfcRequestString);
	public static final UUID characteristicTrip = UUID.fromString(Uuid.characteristicTripString);

}
