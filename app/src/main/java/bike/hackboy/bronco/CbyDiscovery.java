package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;

public class CbyDiscovery extends Fragment {
	ArrayList<BluetoothDevice> matchingDevices = new ArrayList<>();
	protected RecyclerView recyclerViewDevices;
	protected DeviceListAdapter deviceListAdapter;

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			//Log.d("event", event);

			if ("on-discovered".equals(event)) {
				NavHostFragment.findNavController(CbyDiscovery.this)
					.navigate(R.id.action_CbyDiscovery_to_Dashboard);
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));
	}

	@Override
	public void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(requireContext())
			.unregisterReceiver(messageReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView =  inflater.inflate(R.layout.discovery, container, false);
		recyclerViewDevices = rootView.findViewById(R.id.items_list);

		((MainActivity) requireActivity()).getSupportActionBar().setTitle(R.string.app_name);

		return rootView;
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		deviceListAdapter = new DeviceListAdapter(requireContext(), matchingDevices);
		deviceListAdapter.setOnDeviceClickListener(new DeviceListAdapter.onDeviceClickListener() {
			@Override
			public void onClick(String mac) {
				connect(mac);
			}
		});

		recyclerViewDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewDevices.setAdapter(deviceListAdapter);
		recyclerViewDevices.setItemAnimator(new DefaultItemAnimator());

		view.findViewById(R.id.button_connect).setOnClickListener(view1 -> listDevices());
		listDevices();
	}

	public void listDevices() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			if (isAdded()) Toast.makeText(this.getContext(), R.string.bluetooth_off, Toast.LENGTH_LONG).show();
			return;
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		matchingDevices.clear();

		for(BluetoothDevice device : pairedDevices) {
			if(device.getName().equals("COWBOY")) {
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
				matchingDevices.add(device);
			}
		}

		deviceListAdapter.notifyDataSetChanged();
		//Log.d("devices", matchingDevices.toString());
	}

	public void connect(String mac) {
		//Log.d("connect", mac);

		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "connect");
		intent.putExtra("mac", mac);
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
	}
}