package bike.hackboy.bronco.bean;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

import bike.hackboy.bronco.utils.Converter;
import bike.hackboy.bronco.utils.DashboardUpdater;

@SuppressWarnings("unused")
public class DashboardBean implements Serializable {
	private boolean lightOn;
	private int rawDistance;
	private int rawBattery;
	private int rawPower;
	private int[] uptime;
	private String speed;
	private String distance;
	private String assistance;
	private String battery;
	private String duration;

	@SuppressLint("DefaultLocale")
	public DashboardBean fromPersistentDashboard(DashboardUpdater.PersistentDashboard d) {
		setLightOn(d.lights == 1);

		setRawDistance(d.distance);
		setRawBattery(d.battery);
		setRawPower(d.power);

		setUptime(Converter.secondsToTime(d.duration));
		setSpeed(String.format("%s", d.speed));
		setBattery(String.format("%d%%", rawBattery));

		setAssistance((d.assistance == 0 || d.assistance == 3) ? "S" : "D");

		if (rawDistance < 1000) {
			setDistance(String.format("%s m", d.distance));
		} else {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat formatter = new DecimalFormat("#.0", symbols);
			formatter.setRoundingMode(RoundingMode.CEILING);

			setDistance(
				String.format(
					"%s km",
					formatter.format((float) rawDistance / 1000)
				)
			);
		}

		if (uptime[0] > 0) {
			setDuration(String.format(
				"%02d:%02d:%02d",
				uptime[0],
				uptime[1],
				uptime[2]
			));
		} else {
			setDuration(String.format(
				"%02d:%02d",
				uptime[1],
				uptime[2]
			));
		}

		return this;
	}

	public int getRawPower() {
		return rawPower;
	}

	public void setRawPower(int rawPower) {
		this.rawPower = rawPower;
	}

	public int getRawBattery() {
		return rawBattery;
	}

	public void setRawBattery(int rawBattery) {
		this.rawBattery = rawBattery;
	}

	public boolean isLightOn() {
		return lightOn;
	}

	public void setLightOn(boolean lightOn) {
		this.lightOn = lightOn;
	}

	public int getRawDistance() {
		return rawDistance;
	}

	public void setRawDistance(int rawDistance) {
		this.rawDistance = rawDistance;
	}

	public int[] getUptime() {
		return uptime;
	}

	public void setUptime(int[] uptime) {
		this.uptime = uptime;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getAssistance() {
		return assistance;
	}

	public void setAssistance(String assistance) {
		this.assistance = assistance;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	@NonNull
	public String toString() {
		return "DashboardBean{" +
			"lightOn=" + lightOn +
			", rawDistance=" + rawDistance +
			", rawBattery=" + rawBattery +
			", rawPower=" + rawPower +
			", uptime=" + Arrays.toString(uptime) +
			", speed='" + speed + '\'' +
			", distance='" + distance + '\'' +
			", assistance='" + assistance + '\'' +
			", battery='" + battery + '\'' +
			", duration='" + duration + '\'' +
			'}';
	}
}