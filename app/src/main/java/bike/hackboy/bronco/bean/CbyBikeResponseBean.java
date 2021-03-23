package bike.hackboy.bronco.bean;

import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CbyBikeResponseBean {
	private float autonomy; //autonomy
	private float batteryCharge; //battery_state_of_charge
	private float pcbBatteryCharge; //pcb_battery_state_of_charge
	private float carbonSaved; //total_co2_saved
	private float totalDistance; //total_distance
	private float positionLat; //position.latitude
	private float positionLon; //position.longitude

	private String market; //sku.market
	private String color; //sku.color
	private String name; //nickname
	private String mac; //mac_address
	private String serial; //serial_number
	private String passkey; //passkey
	private String positionAddress; //position.address

	private Date activatedAt; //activated_at
	private Date seenAt; //seen_at
	private Date positionReceivedAt; //position.received_at

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getPasskey() {
		return passkey;
	}

	public void setPasskey(String passkey) {
		this.passkey = passkey;
	}

	public Date getActivatedAt() {
		return activatedAt;
	}

	public void setActivatedAt(Date activatedAt) {
		this.activatedAt = activatedAt;
	}

	public Date getPositionReceivedAt() {
		return positionReceivedAt;
	}

	public void setPositionReceivedAt(Date positionReceivedAt) {
		this.positionReceivedAt = positionReceivedAt;
	}

	public Date getSeenAt() {
		return seenAt;
	}

	public void setSeenAt(Date seenAt) {
		this.seenAt = seenAt;
	}

	public float getAutonomy() {
		return autonomy;
	}

	public void setAutonomy(float autonomy) {
		this.autonomy = autonomy;
	}

	public float getBatteryCharge() {
		return batteryCharge;
	}

	public void setBatteryCharge(float batteryCharge) {
		this.batteryCharge = batteryCharge;
	}

	public float getPcbBatteryCharge() {
		return pcbBatteryCharge;
	}

	public void setPcbBatteryCharge(float pcbBatteryCharge) {
		this.pcbBatteryCharge = pcbBatteryCharge;
	}

	public float getCarbonSaved() {
		return carbonSaved;
	}

	public void setCarbonSaved(float carbonSaved) {
		this.carbonSaved = carbonSaved;
	}

	public float getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(float totalDistance) {
		this.totalDistance = totalDistance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPositionAddress() {
		return positionAddress;
	}

	public void setPositionAddress(String positionAddress) {
		this.positionAddress = positionAddress;
	}

	public float getPositionLat() {
		return positionLat;
	}

	public void setPositionLat(float positionLat) {
		this.positionLat = positionLat;
	}

	public float getPositionLon() {
		return positionLon;
	}

	public void setPositionLon(float positionLon) {
		this.positionLon = positionLon;
	}

	@NotNull
	@Override
	public String toString() {
		return "CbyBikeResponseBean{" +
			"autonomy=" + autonomy +
			", batteryCharge=" + batteryCharge +
			", pcbBatteryCharge=" + pcbBatteryCharge +
			", carbonSaved=" + carbonSaved +
			", totalDistance=" + totalDistance +
			", positionLat=" + positionLat +
			", positionLon=" + positionLon +
			", market='" + market + '\'' +
			", color='" + color + '\'' +
			", name='" + name + '\'' +
			", mac='" + mac + '\'' +
			", serial='" + serial + '\'' +
			", passkey='" + passkey + '\'' +
			", positionAddress='" + positionAddress + '\'' +
			", activatedAt=" + activatedAt +
			", seenAt=" + seenAt +
			", positionReceivedAt=" + positionReceivedAt +
			'}';
	}

	public List<PropertiesBean> toPropertiesList() {
		List<PropertiesBean> list = new ArrayList<>();

		PropertiesBean entry;

		entry = new PropertiesBean();
		entry.setName("Seen on");
		entry.setValue(seenAt.toString());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Seen at");
		entry.setValue(positionAddress);
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Latitude");
		entry.setValue(formatDecimal(positionLat));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Longitude");
		entry.setValue(formatDecimal(positionLon));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Last position update");
		entry.setValue(positionReceivedAt.toString());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Total distance");
		entry.setValue(String.format("%s km", formatDecimal(totalDistance)));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("CO2 saved");
		entry.setValue(String.format("%s kg", Math.floor(pcbBatteryCharge)));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Battery");
		entry.setValue(String.format("%s%%", Math.floor(batteryCharge)));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("PCB battery");
		entry.setValue(String.format("%s%%", Math.floor(pcbBatteryCharge)));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Range");
		entry.setValue(String.format("%s km", formatDecimal(autonomy)));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Serial number");
		entry.setValue(serial);
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Color");
		entry.setValue(color);
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("MAC address");
		entry.setValue(mac);
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Activated at");
		entry.setValue(activatedAt.toString());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Market");
		entry.setValue(market);
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("Pairing code");
		entry.setValue(passkey);
		entry.setLast(true);
		list.add(entry);

		return list;
	}

	private String formatDecimal(float number) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat decimalFormat = new DecimalFormat("#.####", symbols);
		decimalFormat.setRoundingMode(RoundingMode.CEILING);
		return decimalFormat.format(number);
	}

}
