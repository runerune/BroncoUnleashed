package bike.hackboy.bronco.view;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import bike.hackboy.bronco.R;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
	private final List<BluetoothDevice> data;
	private final LayoutInflater inflater;
	onDeviceClickListener onDeviceClickListener;

	public abstract static class onDeviceClickListener {
		public void onClick(String mac) { }
	}

	public DeviceListAdapter(Context context, List<BluetoothDevice> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.device_item, parent, false);
		return new ViewHolder(view);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
		BluetoothDevice device = data.get(position);

		String name = device.getName();
		String alias = null;
		String mac = device.getAddress();

        /* min Android API level required is 30 (R) */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            alias = device.getAlias();
        }

		holder.name.setText(name);
		holder.mac.setText(mac);

		if(alias != null && !alias.isEmpty() && !alias.equals(name)) {
			holder.name.setText(alias);
		} else if(name != null && !name.isEmpty()) {
			holder.name.setText(name);
		} else {
			// idk...
			holder.name.setText("COWBOY");
		}

		if (onDeviceClickListener != null) {
			holder.container.setOnClickListener(v -> onDeviceClickListener.onClick(mac));
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public void setOnDeviceClickListener(onDeviceClickListener onDeviceClickListener) {
		this.onDeviceClickListener = onDeviceClickListener;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView name;
		final TextView mac;
		final ConstraintLayout container;

		ViewHolder(View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.name);
			mac = itemView.findViewById(R.id.mac);
			container = itemView.findViewById(R.id.device_item);
		}
	}
}
