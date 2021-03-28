package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.FlashWriter;

public class FieldWeakening extends Fragment {
	protected int weakening = 0;
	protected int weakeningAsiValue = 0;
	protected boolean commitWrite = false;

	protected static final double ASI_FIELD_WEAKENING_MULTIPLIER = 40.96;

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

			//Log.d("value", Converter.byteArrayToHexString(value));

			if(service == 0x1) {
				switch(operation) {
					case 0x3: // read notification
						int rawWeakening = (value[3] << 8) + (value[4] & 0xff);
						//Log.d("rawWeakening", String.valueOf(rawWeakening));
						FieldWeakening.this.weakening = (int) Math.ceil(rawWeakening / ASI_FIELD_WEAKENING_MULTIPLIER);

						updateAsiValue();
						updateView();
					break;
					case 0x10: // write success notification
						if (commitWrite) {
							//Log.d("write_flash", Converter.byteArrayToHexString(value));
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
		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-field-weakening"));

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

		bar.setTitle(R.string.field_weakening);
		bar.setDisplayHomeAsUpEnabled(true);

		return inflater.inflate(R.layout.field_weakening, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_field_weakening_apply).setOnClickListener(view1 -> {
			if(weakeningAsiValue > 0) {
				new AlertDialog.Builder(requireContext(), R.style.Theme_Bronco_AlertDialogWarning)
					.setTitle(R.string.caution_battery)
					.setMessage(R.string.field_weakening_disclaimer)
					.setNegativeButton(R.string.abort, null)
					.setPositiveButton(R.string.proceed, (dialog, whichButton) -> {
						dialog.dismiss();
						writeValue();
					})
					.show();
			} else {
				writeValue();
			}
		});

		view.findViewById(R.id.button_field_weakening_cancel).setOnClickListener(view1 -> NavHostFragment
			.findNavController(FieldWeakening.this)
			.navigate(R.id.action_FieldWeakening_to_Settings));


		SeekBar slider = view.findViewById(R.id.field_weakening_bar);
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				TextView percentage = view.findViewById(R.id.field_weakening_value);

				weakening = progress;
				updateAsiValue();

				percentage.setText(String.valueOf(weakening));
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
		//Log.d("weakening_value", String.valueOf(weakeningAsiValue));

		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
			.putExtra("event", "set-field-weakening")
			.putExtra("value", weakeningAsiValue));

		//NavHostFragment.findNavController(FieldWeakening.this).navigate(R.id.action_FieldWeakening_to_Dashboard);
	}

	private void updateView() {
		View view = requireView();

		view.findViewById(R.id.reading_from_bike).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.group_editor).setVisibility(View.VISIBLE);

		((TextView) view.findViewById(R.id.field_weakening_value)).setText(String.valueOf(weakening));
		((SeekBar) view.findViewById(R.id.field_weakening_bar)).setProgress(weakening);

	}

	private void writeFlash() {
		commitWrite = false;
		FlashWriter fw = new FlashWriter(requireContext());
		fw.run();
	}

	private void updateAsiValue() {
		weakeningAsiValue = (int) Math.floor(ASI_FIELD_WEAKENING_MULTIPLIER * weakening);
	}
}
