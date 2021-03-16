package bike.hackboy.bronco;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;


public class CbyDiscovery extends Fragment {
	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			//Log.d("event", event);

			switch (event) {
				case "on-discovered":
					NavHostFragment.findNavController(CbyDiscovery.this)
						.navigate(R.id.action_CbyDiscovery_to_Dashboard);
					break;
				case "toast":
					Toast.makeText(getActivity(), intent.getStringExtra("message"), Toast.LENGTH_LONG).show();
					break;
			}
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(requireContext())
			.unregisterReceiver(messageReceiver);
	}

	@Override
	public View onCreateView(
		LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.discovery, container, false);
	}

	public void lookForCboy() {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "connect");
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_connect).setOnClickListener(view1 -> lookForCboy());
	}
}