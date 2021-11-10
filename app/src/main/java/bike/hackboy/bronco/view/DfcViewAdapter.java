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
import bike.hackboy.bronco.utils.Converter;

public class DfcViewAdapter extends RecyclerView.Adapter<DfcViewAdapter.ViewHolder> {
	private final List<byte[]> data;
	private final LayoutInflater inflater;

	public DfcViewAdapter(Context context, List<byte[]> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.dfc_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		byte[] entry = data.get(position);

		holder.value.setText(Converter.byteArrayToHexString(entry));

		if ((position + 1) == data.size()) {
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
		final TextView value;
		final TextView divider;
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			value = itemView.findViewById(R.id.value);
			divider = itemView.findViewById(R.id.divider);
			container = itemView.findViewById(R.id.bike_data_item);
		}
	}
}