package bike.hackboy.bronco.bean;

public class PropertiesBean {
	private String name;
	private String value;

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

	@Override
	public String toString() {
		return "PropertiesBean{" +
			"name='" + name + '\'' +
			", value='" + value + '\'' +
			'}';
	}
}
