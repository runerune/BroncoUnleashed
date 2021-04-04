package bike.hackboy.bronco.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bike.hackboy.bronco.R;
import bike.hackboy.bronco.bean.CbyBikeResponseBean;
import bike.hackboy.bronco.bean.BikePropertyBean;

public class BikePropertyFactory {
	public static List<BikePropertyBean> fromBikeBean(CbyBikeResponseBean bean) {
		List<BikePropertyBean> list = new ArrayList<>();

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

		BikePropertyBean entry;

		entry = new BikePropertyBean();
		entry.setName(R.string.bike_name);
		entry.setValue(bean.getName());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.last_seen);
		entry.setValue(pt.format(bean.getPositionReceivedAt()));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.location);
		entry.setValue(bean.getPositionAddress());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.coords);
		entry.setValue(combinedLatLon);
		entry.setLink(String.format("geo:%s,%s", lat, lon));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.total_distance);
		entry.setValue(String.format("%s km", Converter.formatDecimal(bean.getTotalDistance())));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.co2_saved);
		entry.setValue(String.format("%s kg", (int) bean.getCarbonSaved()/1000));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.bike_battery);
		entry.setValue(String.format("%s%%", (int) bean.getBatteryCharge()));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.pcb_battery);
		entry.setValue(String.format("%s%%", (int) bean.getPcbBatteryCharge()));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.range_estimate);
		entry.setValue(String.format("%s km", Converter.formatDecimal(bean.getAutonomy())));
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.serial_number);
		entry.setValue(bean.getSerial());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.color);
		entry.setValue(bean.getColor());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.mac_address);
		entry.setValue(bean.getMac());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.firmware_version);
		entry.setValue(bean.getFirmwareVersion());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.activated_at);
		entry.setValue(bean.getActivatedAt());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.market);
		entry.setValue(bean.getMarket());
		list.add(entry);

		entry = new BikePropertyBean();
		entry.setName(R.string.pairing_code);
		entry.setValue(bean.getPasskey());
		entry.setLast(true);
		list.add(entry);

		return list;
	}
}
