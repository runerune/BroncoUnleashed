package bike.hackboy.bronco.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bike.hackboy.bronco.bean.CbyBikeResponseBean;
import bike.hackboy.bronco.bean.PropertiesBean;

public class BikePropertyBuilder {
	public static List<PropertiesBean> fromBikeBean(CbyBikeResponseBean bean) {
		List<PropertiesBean> list = new ArrayList<>();

		PrettyTime pt = new PrettyTime();
		pt.setLocale(Locale.US);

		// ------------------------------------------------------

		float lat = bean.getPositionLat();
		float lon = bean.getPositionLon();

		String combinedLatLon = String.format(
			"%s %s, %s %s",
			Converter.floatCoordsToDegrees(lat),
			lat > 0 ? "N" : "S",
			Converter.floatCoordsToDegrees(lon),
			lon > 0 ? "E" : "W"
		);

		// ------------------------------------------------------

		PropertiesBean entry;

		entry = new PropertiesBean();
		entry.setName("last_seen");
		entry.setValue(pt.format(bean.getPositionReceivedAt()));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("location");
		entry.setValue(bean.getPositionAddress());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("coords");
		entry.setValue(combinedLatLon);
		entry.setLink(String.format("geo:%s,%s", lat, lon));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("total_distance");
		entry.setValue(String.format("%s km", Converter.formatDecimal(bean.getTotalDistance())));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("co2_saved");
		entry.setValue(String.format("%s kg", (int) bean.getCarbonSaved()/1000));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("bike_battery");
		entry.setValue(String.format("%s%%", (int) bean.getBatteryCharge()));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("pcb_battery");
		entry.setValue(String.format("%s%%", (int) bean.getPcbBatteryCharge()));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("range_estimate");
		entry.setValue(String.format("%s km", Converter.formatDecimal(bean.getAutonomy())));
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("serial_number");
		entry.setValue(bean.getSerial());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("color");
		entry.setValue(bean.getColor());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("mac_address");
		entry.setValue(bean.getMac());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("firmware_version");
		entry.setValue(bean.getFirmwareVersion());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("activated_at");
		entry.setValue(bean.getActivatedAt().toString());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("market");
		entry.setValue(bean.getMarket());
		list.add(entry);

		entry = new PropertiesBean();
		entry.setName("pairing_code");
		entry.setValue(bean.getPasskey());
		entry.setLast(true);
		list.add(entry);

		return list;
	}
}
