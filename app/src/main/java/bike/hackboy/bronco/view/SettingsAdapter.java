package bike.hackboy.bronco.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import java.util.List;

import bike.hackboy.bronco.R;
import bike.hackboy.bronco.bean.SettingBean;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	protected final List<SettingBean> settings;
	protected final LayoutInflater inflater;

	public SettingsAdapter(Context context, List<SettingBean> data) {
		this.inflater = LayoutInflater.from(context);
		this.settings = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.settings_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SettingBean entry = settings.get(position);

		holder.name.setText(entry.getName());
		holder.description.setText(entry.getDescription());

		if(entry.isHasArrow()) {
			holder.setIsRecyclable(false);
			holder.arrow.setVisibility(View.VISIBLE);
		} else if (entry.getValue() != null) {
			holder.setIsRecyclable(false);

			if(entry.getValue().isEmpty()) {
				holder.loader.setVisibility(View.VISIBLE);
				holder.value.setVisibility(View.GONE);
			} else {
				holder.value.setText(entry.getValue());
				holder.value.setVisibility(View.VISIBLE);
				holder.loader.setVisibility(View.GONE);
			}
		}

		holder.container.setOnClickListener(view -> {
			View.OnClickListener listener = entry.getOnClickListener();
			listener.onClick(view);
		});
	}

	@Override
	public int getItemCount() {
		return settings.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView name;
		final TextView description;
		final ImageView arrow;
		final TextView value;
		final ProgressBar loader;
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.settings_name);
			description = itemView.findViewById(R.id.settings_description);
			arrow = itemView.findViewById(R.id.settings_arrow);
			value = itemView.findViewById(R.id.settings_value);
			loader = itemView.findViewById(R.id.settings_loader);
			container = itemView.findViewById(R.id.settings_item);
		}
	}
}