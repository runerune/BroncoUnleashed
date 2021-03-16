package bike.hackboy.bronco;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

public class SpeedSetting extends Fragment {
	private int value = 25;

	@Override
	public View onCreateView(
		LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {
		return inflater.inflate(R.layout.speed_setting, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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

		SeekBar slider = view.findViewById(R.id.seekBar);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				TextView kph = view.findViewById(R.id.textView3);
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
}