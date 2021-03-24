package bike.hackboy.bronco.bean;

public class PropertiesBean {
	private String name;
	private String value;
	private boolean last = false;
	private boolean isGeoLink = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public boolean isGeoLink() {
		return isGeoLink;
	}

	public void setGeoLink(boolean geoLink) {
		isGeoLink = geoLink;
	}

	@Override
	public String toString() {
		return "PropertiesBean{" +
			"name='" + name + '\'' +
			", value='" + value + '\'' +
			", last=" + last +
			", isGeoLink=" + isGeoLink +
			'}';
	}
}
