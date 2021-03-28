package bike.hackboy.bronco.bean;

import android.view.View;

import org.jetbrains.annotations.NotNull;

public class SettingsEntityBean {
	private String name;
	private String description;
	private View.OnClickListener onClickListener;
	private boolean hasArrow;

	public String getName() {
		return name;
	}

	public SettingsEntityBean setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public SettingsEntityBean setDescription(String description) {
		this.description = description;
		return this;
	}

	public View.OnClickListener getOnClickListener() {
		return onClickListener;
	}

	public SettingsEntityBean setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	public boolean isHasArrow() {
		return hasArrow;
	}

	public SettingsEntityBean setHasArrow(boolean hasArrow) {
		this.hasArrow = hasArrow;
		return this;
	}

	@NotNull
	@Override
	public String toString() {
		return "SettingsEntityBean{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", onClickListener=" + onClickListener +
			", hasArrow=" + hasArrow +
			'}';
	}
}
