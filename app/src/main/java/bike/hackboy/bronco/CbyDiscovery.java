package bike.hackboy.bronco;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;

import bike.hackboy.bronco.view.DeviceListAdapter;

public class CbyDiscovery extends Fragment {
	protected final ArrayList<BluetoothDevice> matchingDevices = new ArrayList<>();
	protected RecyclerView recyclerViewDevices;
	protected DeviceListAdapter deviceListAdapter;
	protected boolean firstRun = true;

	private final Handler loaderThreadHandler = new Handler(Looper.getMainLooper());
	private final Runnable hideLoader = () ->
		requireView().findViewById(R.id.loader).setVisibility(View.INVISIBLE);

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
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

		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;
		
		bar.setTitle(R.string.app_name);
		bar.setDisplayHomeAsUpEnabled(false);

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

		view.findViewById(R.id.button_connect).setOnClickListener(v -> listDevices());
		listDevices();
	}

	protected void listDevices() {
		BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);

		requireView().findViewById(R.id.bluetooth_off).setVisibility(View.INVISIBLE);
		requireView().findViewById(R.id.no_devices).setVisibility(View.INVISIBLE);
		requireView().findViewById(R.id.items_list).setVisibility(View.INVISIBLE);

		matchingDevices.clear();

		if (!bluetoothManager.getAdapter().isEnabled()) {
			requireView().findViewById(R.id.bluetooth_off).setVisibility(View.VISIBLE);
			return;
		}

		if(!firstRun) showPlaceboLoader();
		firstRun = false;

		Set<BluetoothDevice> pairedDevices = bluetoothManager.getAdapter().getBondedDevices();

		for(BluetoothDevice device : pairedDevices) {
			if(device.getName().equals("COWBOY")) {
				matchingDevices.add(device);
			}
		}

		if (matchingDevices.size() < 1) {
			requireView().findViewById(R.id.no_devices).setVisibility(View.VISIBLE);
			return;
		}

		requireView().findViewById(R.id.items_list).setVisibility(View.VISIBLE);
		deviceListAdapter.notifyDataSetChanged();
		//Log.d("devices", matchingDevices.toString());
	}

	protected void connect(String mac) {
		BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);

		if (!bluetoothManager.getAdapter().isEnabled()) {
			requireView().findViewById(R.id.items_list).setVisibility(View.INVISIBLE);
			requireView().findViewById(R.id.bluetooth_off).setVisibility(View.VISIBLE);
			return;
		}

		//Log.d("connect", mac);

		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "connect");
		intent.putExtra("mac", mac);
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
	}

	// listing is instant but let's give some fake feedback to the user so
	// they don't think nothing happened if the list stays the same
	protected void showPlaceboLoader() {
		loaderThreadHandler.removeCallbacks(hideLoader);

		requireView().findViewById(R.id.loader).setVisibility(View.VISIBLE);
		loaderThreadHandler.postDelayed(hideLoader, (int) (Math.random()*400 + 400));
	}
}