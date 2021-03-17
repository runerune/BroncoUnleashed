package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import bike.hackboy.bronco.data.Uuid;

public class SpeedSetting extends Fragment {
	private int value = 25;

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		String event = intent.getStringExtra("event");

		if(!event.equals("on-characteristic-read")) return;
		if(!intent.getStringExtra("uuid").equals(Uuid.characteristicSettingsReadString)) return;

		byte[] value = intent.getByteArrayExtra("value");

		SpeedSetting.this.value = value[4];
		updateValue();
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
		return inflater.inflate(R.layout.speed_setting, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// don't show this until we get the value from the bike
		view.findViewById(R.id.speed_setting).setVisibility(View.INVISIBLE);

		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-speed")
		);

		view.findViewById(R.id.button_speed_apply).setOnClickListener(view1 -> {
			LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
				new Intent(BuildConfig.APPLICATION_ID)
					.putExtra("event", "set-speed")
					.putExtra("value", value)
			);

			NavHostFragment.findNavController(SpeedSetting.this).navigate(R.id.action_SpeedSetting_to_Dashboard);
		});

		view.findViewById(R.id.button_speed_cancel).setOnClickListener(view1 -> NavHostFragment
			.findNavController(SpeedSetting.this)
			.navigate(R.id.action_SpeedSetting_to_Dashboard));

		SeekBar slider = view.findViewById(R.id.max_speed_bar);
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				TextView kph = view.findViewById(R.id.max_speed_value);
				value = 25 + progress;
				kph.setText(String.valueOf(value));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void updateValue() {
		try {
			((SeekBar) getView().findViewById(R.id.max_speed_bar)).setProgress(value - 25);
			getView().findViewById(R.id.speed_setting).setVisibility(View.VISIBLE);
		} catch(Exception e) {
			//
		}
	}
}