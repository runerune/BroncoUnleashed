package bike.hackboy.bronco.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import bike.hackboy.bronco.DfcProto;
import bike.hackboy.bronco.R;

public class DfcViewAdapter extends RecyclerView.Adapter<DfcViewAdapter.ViewHolder> {
	private final List<DfcProto.Dfc> data;
	private final LayoutInflater inflater;

	private PrettyTime pt = new PrettyTime();

	public DfcViewAdapter(Context context, List<DfcProto.Dfc> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;

		pt.setLocale(Locale.US);
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.dfc_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		DfcProto.Dfc entry = data.get(position);
		Date dfcDate = new Date((long) entry.getTimestamp() * 1000);

		holder.title.setText(entry.getDfcId().toString());
		//holder.details.setText(entry.toString());

		holder.details.setText(pt.format(dfcDate));

		if ((position + 1) == data.size()) {
			holder.setIsRecyclable(false);
			holder.divider.setVisibility(View.INVISIBLE);
			holder.details.setPadding(0, 0, 0, 164);
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView title;
		final TextView details;
		final TextView divider;
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			details = itemView.findViewById(R.id.details);
			divider = itemView.findViewById(R.id.divider);
			container = itemView.findViewById(R.id.bike_data_item);
		}
	}
}