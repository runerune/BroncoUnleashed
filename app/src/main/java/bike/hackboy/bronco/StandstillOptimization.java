package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.FlashWriter;

public class StandstillOptimization extends Fragment {
	protected int transitions = 0;
	protected boolean commitWrite = false;

	protected final int[] descriptions = new int[] {
		R.string.rough,
		R.string.medium,
		R.string.soft
	};

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");

			if (!event.equals("on-characteristic-read")) return;
			if (!intent.getStringExtra("uuid").equals(Uuid.characteristicSettingsReadString))
				return;

			byte[] value = intent.getByteArrayExtra("value");
			int service = value[0];
			int operation = value[1];

			if(service == 0x1) {
				switch(operation) {
					case 0x3:
						transitions = (value[3] << 8) + (value[4] & 0xff);

						if(transitions > 15) transitions = 1;
						if(transitions < 1) transitions = 1;

						updateView();
					break;
					case 0x10:
						if (commitWrite) {
							writeFlash();
						}
					break;
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(requireContext());
		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-hall-transitions"));

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
		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.standstill_optimization);
		bar.setDisplayHomeAsUpEnabled(true);

		return inflater.inflate(R.layout.standstill_optimization, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_standstill_optimization_apply).setOnClickListener(v -> writeValue());

		view.findViewById(R.id.button_standstill_optimization_cancel).setOnClickListener(v -> NavHostFragment
			.findNavController(StandstillOptimization.this)
			.navigate(R.id.action_standstillOptimization_to_Settings));

		SeekBar slider = view.findViewById(R.id.standstill_optimization_ui_value);
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(progress == 0) transitions = 1;
				if(progress == 1) transitions = 8;
				if(progress == 2) transitions = 15;

				((TextView) view.findViewById(R.id.standstill_optimization_ui_description)).setText(
					getText(descriptions[progress])
				);

				((SeekBar) view.findViewById(R.id.standstill_optimization_ui_value)).setProgress(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	private void writeValue() {
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(requireContext());
		commitWrite = true;

		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
			.putExtra("event", "set-hall-transitions")
			.putExtra("value", transitions));
	}

	private void updateView() {
		View view = requireView();

		view.findViewById(R.id.standstill_reading_from_bike).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.torque_standstill_group).setVisibility(View.VISIBLE);

		int selectedOption = 0;
		if(transitions > 1) selectedOption = 1;
		if(transitions > 8) selectedOption = 2;

		((TextView) view.findViewById(R.id.standstill_optimization_ui_description)).setText(
			getText(descriptions[selectedOption])
		);
		((SeekBar) view.findViewById(R.id.standstill_optimization_ui_value)).setProgress(selectedOption);
	}

	private void writeFlash() {
		commitWrite = false;
		FlashWriter fw = new FlashWriter(requireContext());

		fw.setOnWriteAction(new FlashWriter.OnWriteAction() {
			@Override
			public void onComplete() {
				requireActivity().runOnUiThread(() -> NavHostFragment
					.findNavController(StandstillOptimization.this)
					.navigate(R.id.action_standstillOptimization_to_Settings));
			}

			@Override
			public void onError() {

			}
		});

		fw.run();
	}
}