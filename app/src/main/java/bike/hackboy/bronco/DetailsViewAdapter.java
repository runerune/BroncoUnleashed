package bike.hackboy.bronco;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import bike.hackboy.bronco.bean.PropertiesBean;

public class DetailsViewAdapter extends RecyclerView.Adapter<DetailsViewAdapter.ViewHolder> {
	private final List<PropertiesBean> data;
	private final LayoutInflater inflater;

	DetailsViewAdapter(Context context, List<PropertiesBean> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.list_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		PropertiesBean entry = data.get(position);
		holder.name.setText(entry.getName());
		holder.value.setText(entry.getValue());
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		TextView value;

		ViewHolder(View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.name);
			value = itemView.findViewById(R.id.value);
		}
	}
}