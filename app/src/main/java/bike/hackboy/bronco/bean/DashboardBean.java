package bike.hackboy.bronco.bean;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.Arrays;

import bike.hackboy.bronco.DashboardProto;
import bike.hackboy.bronco.utils.Converter;

public class DashboardBean {
	public boolean lightOn;
	public int rawDistance;
	public int rawBattery;
	public int rawPower;
	public int[] uptime;
	public String speed;
	public String distance;
	public String assistance;
	public String battery;
	public String duration;

	@SuppressLint("DefaultLocale")
	public DashboardBean fromProtobuf(DashboardProto.Dashboard d) {
		setLightOn(d.getLights() == 1);

		setRawDistance(d.getDistance());
		setRawBattery(d.getBattery());
		setRawPower(d.getPower());

		setUptime(Converter.secondsToTime(d.getDuration()));
		setSpeed(String.format("%s", d.getSpeed()));
		setBattery(String.format("%d%%", rawBattery));

		setAssistance((d.getAssistance() == 0 || d.getAssistance() == 3) ? "S" : "D");

		if (rawDistance < 1000) {
			setDistance(String.format("%s m", d.getDistance()));
		} else {
			DecimalFormat formatter = new DecimalFormat("#,###.00");
			setDistance(formatter.format(d.getDistance() / 1000));
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