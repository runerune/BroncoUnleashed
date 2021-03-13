package bike.hackboy.bronco.data;

import java.util.UUID;

public class Uuid {
    public static final String serviceSettingsString = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String serviceUnlockString = "C0B0A000-18EB-499D-B266-2F2910744274";

    public static final String characteristicSettingsPcbString = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String characteristicUnlockString = "C0B0A001-18EB-499D-B266-2F2910744274";

    public static final UUID serviceSettings = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID serviceUnlock = UUID.fromString("C0B0A000-18EB-499D-B266-2F2910744274");

    public static final UUID characteristicSettingsPcb = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID characteristicUnlock = UUID.fromString("C0B0A001-18EB-499D-B266-2F2910744274");
}
