package bike.hackboy.bronco.bean;

import org.jetbrains.annotations.NotNull;

public class BikePropertyBean {
	private String name;
	private String value;
	private String link;
	private boolean last = false;

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

	public String getLink() {
		return this.link;
	}

	public boolean hasLink() {
		return this.link != null && !this.link.isEmpty();
	}

	public void setLink(String link) {
		this.link = link;
	}

	@NotNull
	@Override
	public String toString() {
		return "PropertiesBean{" +
			"name='" + name + '\'' +
			", value='" + value + '\'' +
			", last=" + last +
			", link=" + link +
			'}';
	}
}
