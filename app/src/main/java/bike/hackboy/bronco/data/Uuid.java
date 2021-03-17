package bike.hackboy.bronco.data;

import java.util.UUID;

public class Uuid {
	public static final String characteristicUnlockString = "C0B0A001-18EB-499D-B266-2F2910744274";
	public static final String characteristicDashboardString = "C0B0A00A-18EB-499D-B266-2F2910744274";
	public static final String characteristicSettingsReadString = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
	public static final String characteristicSettingsWriteString = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";

	public static final UUID serviceSettings = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	public static final UUID serviceUnlock = UUID.fromString("C0B0A000-18EB-499D-B266-2F2910744274");

	public static final UUID characteristicSettingsWrite = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
	public static final UUID characteristicSettingsRead = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
	public static final UUID characteristicUnlock = UUID.fromString("C0B0A001-18EB-499D-B266-2F2910744274");
	public static final UUID characteristicDashboard = UUID.fromString("C0B0A00A-18EB-499D-B266-2F2910744274");

}
