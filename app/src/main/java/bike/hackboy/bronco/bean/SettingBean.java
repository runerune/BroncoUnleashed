package bike.hackboy.bronco.bean;

import android.view.View;

import org.jetbrains.annotations.NotNull;

public class SettingBean {
	private String name;
	private String description;
	private View.OnClickListener onClickListener;
	private boolean hasArrow;
	private String value;

	public String getName() {
		return name;
	}

	public SettingBean setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public SettingBean setDescription(String description) {
		this.description = description;
		return this;
	}

	public View.OnClickListener getOnClickListener() {
		return onClickListener;
	}

	public SettingBean setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	public boolean isHasArrow() {
		return hasArrow;
	}

	public SettingBean setHasArrow(boolean hasArrow) {
		this.hasArrow = hasArrow;
		return this;
	}

	public String getValue() {
		return value;
	}

	public SettingBean setValue(String value) {
		this.value = value;
		return this;
	}

	@NotNull
	@Override
	public String toString() {
		return "SettingBean{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", onClickListener=" + onClickListener +
			", hasArrow=" + hasArrow +
			", value=" + value +
			'}';
	}
}
