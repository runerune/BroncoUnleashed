package bike.hackboy.bronco.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import bike.hackboy.bronco.R;
import bike.hackboy.bronco.bean.BikePropertyBean;

public class DetailsViewAdapter extends RecyclerView.Adapter<DetailsViewAdapter.ViewHolder> {
	private final List<BikePropertyBean> data;
	private final LayoutInflater inflater;

	public DetailsViewAdapter(Context context, List<BikePropertyBean> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.bike_data_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BikePropertyBean entry = data.get(position);
		Context context = holder.itemView.getContext();

		holder.name.setText(context.getString(entry.getName()));
		holder.value.setText(entry.getValue());
		holder.value.setText(entry.getValue());

		if(entry.hasLink()) {
			SpannableString spanStr = new SpannableString(entry.getValue());
			spanStr.setSpan(new UnderlineSpan(), 0, spanStr.length(), 0);
			holder.value.setText(spanStr);

			holder.container.setOnClickListener(v -> {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
				context.startActivity(intent);
			});

			holder.arrow.setVisibility(View.VISIBLE);
		} else {
			holder.arrow.setVisibility(View.GONE);
			holder.value.setText(entry.getValue());
		}

		if (entry.isLast()) {
			holder.setIsRecyclable(false);
			holder.divider.setVisibility(View.INVISIBLE);
			holder.value.setPadding(0, 0, 0, 164);
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView name;
		final TextView value;
		final TextView divider;
		final ImageView arrow;
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.name);
			value = itemView.findViewById(R.id.value);
			divider = itemView.findViewById(R.id.divider);
			arrow = itemView.findViewById(R.id.arrow);
			container = itemView.findViewById(R.id.bike_data_item);
		}
	}
}