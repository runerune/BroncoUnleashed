package bike.hackboy.bronco;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import java.util.List;

import bike.hackboy.bronco.bean.SettingsEntityBean;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private final List<SettingsEntityBean> settings;
	private final LayoutInflater inflater;

	SettingsAdapter(Context context, List<SettingsEntityBean> data) {
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
		SettingsEntityBean entry = settings.get(position);

		holder.name.setText(entry.getName());
		holder.description.setText(entry.getDescription());

		holder.arrow.setVisibility(entry.isHasArrow() ? View.VISIBLE : View.GONE);

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
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.settings_name);
			description = itemView.findViewById(R.id.settings_description);
			arrow = itemView.findViewById(R.id.settings_arrow);
			container = itemView.findViewById(R.id.settings_item);
		}
	}
}