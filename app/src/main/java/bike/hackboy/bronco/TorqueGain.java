package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.Converter;
import bike.hackboy.bronco.utils.FlashWriter;

public class TorqueGain extends Fragment {
	protected int gain = 0;
	protected int asiGain = 0;
	protected boolean commitWrite = false;

	//1024
	protected static final double ASI_MULTIPLIER = 102.4;

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

			Log.d("value", Converter.byteArrayToHexString(value));

			if(service == 0x1) {
				switch(operation) {
					case 0x3: // read notification
						int rawGain = (value[3] << 8) + (value[4] & 0xff);
						Log.d("torque_gain", String.valueOf(rawGain));
						TorqueGain.this.gain = (int) Math.ceil(rawGain / ASI_MULTIPLIER);

						updateAsiValue();
						updateView();
					break;
					case 0x10: // write success notification
						if (commitWrite) {
							Log.d("write_flash", Converter.byteArrayToHexString(value));
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
		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-torque-gain"));

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

		bar.setTitle(R.string.torque_gain);
		bar.setDisplayHomeAsUpEnabled(true);

		return inflater.inflate(R.layout.torque_gain, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_torque_gain_apply).setOnClickListener(view1 -> {
			writeValue();
		});

		view.findViewById(R.id.button_torque_gain_cancel).setOnClickListener(view1 -> NavHostFragment
			.findNavController(TorqueGain.this)
			.navigate(R.id.action_torqueGain_to_Settings));


		SeekBar slider = view.findViewById(R.id.torque_gain_bar);
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				TextView percentage = view.findViewById(R.id.torque_gain_value);

				gain = progress;
				updateAsiValue();

				percentage.setText(String.valueOf(gain));
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
			.putExtra("event", "set-torque-gain")
			.putExtra("value", asiGain));
	}

	private void updateView() {
		View view = requireView();

		view.findViewById(R.id.torque_gain_reading_from_bike).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.torque_gain_group_editor).setVisibility(View.VISIBLE);

		((TextView) view.findViewById(R.id.torque_gain_value)).setText(String.valueOf(gain));
		((SeekBar) view.findViewById(R.id.torque_gain_bar)).setProgress(gain);
	}

	private void updateAsiValue() {
		asiGain = (int) Math.floor(ASI_MULTIPLIER * gain);
		Log.d("asi_gain", String.valueOf(asiGain));
	}

	private void writeFlash() {
		/*commitWrite = false;
		FlashWriter fw = new FlashWriter(requireContext());

		fw.setOnWriteAction(new FlashWriter.OnWriteAction() {
			@Override
			public void onComplete() {
				requireActivity().runOnUiThread(() -> NavHostFragment
					.findNavController(TorqueGain.this)
					.navigate(R.id.action_torqueGain_to_Settings));
			}

			@Override
			public void onError() {

			}
		});

		fw.run();*/
	}

}