package bike.hackboy.bronco.utils;

import bike.hackboy.bronco.DashboardProto;

public class DashboardUpdater {
	PersistentDashboard instance;

	public class PersistentDashboard {
		public int tripId = 0;
		public int duration = 0;
		public int speed = 0;
		public int power = 0;
		public int distance = 0;
		public int battery = 0;
		public int assistance = 0;
		public int lights = 0;
		public int batteryVoltage = 0;
		public float batteryTemp = 0;
	}

	public DashboardUpdater() {
		this.instance = new PersistentDashboard();
	}

	public PersistentDashboard getCurrent() {
		return this.instance;
	}

	public void forceUpdateLightsOff() {
		// don't ask me why they did this, for faking responsiveness maybe?
		this.instance.lights = 0;
	}

	public void updateFromPacket(DashboardProto.Dashboard packet) {
		//Log.d("dash_packet", packet.toString());

		if(hasField(packet, DashboardProto.Dashboard.TRIPID_FIELD_NUMBER)) {
			this.instance.tripId = packet.getTripId();
		}

		if(hasField(packet, DashboardProto.Dashboard.DURATION_FIELD_NUMBER)) {
			this.instance.duration = packet.getDuration();
		}

		if(hasField(packet, DashboardProto.Dashboard.SPEED_FIELD_NUMBER)) {
			this.instance.speed = packet.getSpeed();
		}

		if(hasField(packet, DashboardProto.Dashboard.POWER_FIELD_NUMBER)) {
			this.instance.power = packet.getPower();
		}

		if(hasField(packet, DashboardProto.Dashboard.DISTANCE_FIELD_NUMBER)) {
			this.instance.speed = packet.getSpeed();
			this.instance.power = packet.getPower();
			this.instance.distance = packet.getDistance();
		}

		if(hasField(packet, DashboardProto.Dashboard.BATTERY_FIELD_NUMBER)) {
			this.instance.battery = packet.getBattery();
		}

		if(
			hasField(packet, DashboardProto.Dashboard.ASSISTANCE_FIELD_NUMBER) ||
			hasField(packet, DashboardProto.Dashboard.LIGHTS_FIELD_NUMBER)
		) {
			this.instance.assistance = packet.getAssistance();
			this.instance.lights = packet.getLights();
		}
	}

	private static boolean hasField(DashboardProto.Dashboard message, int field) {
		return message.hasField(DashboardProto.Dashboard.getDescriptor().findFieldByNumber(field));
	}
}
