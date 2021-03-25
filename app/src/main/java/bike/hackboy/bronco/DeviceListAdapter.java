package bike.hackboy.bronco;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
	private final List<BluetoothDevice> data;
	private final LayoutInflater inflater;
	onDeviceClickListener onDeviceClickListener;

	public abstract static class onDeviceClickListener {
		public void onClick(String mac) { }
	}

	DeviceListAdapter(Context context, List<BluetoothDevice> data) {
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.device_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
		BluetoothDevice device = data.get(position);

		String name = device.getName();
		String alias = device.getAlias();
		String mac = device.getAddress();

		holder.name.setText(name);
		holder.mac.setText(mac);

		if(!alias.isEmpty() && !alias.equals(name)) {
			holder.name.setText(alias);
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