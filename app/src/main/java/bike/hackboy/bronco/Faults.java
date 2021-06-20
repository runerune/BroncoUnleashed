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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.Converter;

public class Faults extends Fragment {
	protected int readingFaultRegisterNumber = 1;
	protected List<String> caughtFaults;

	private static final Map<String, String> faults;

	static {
		Map<String, String> faultMap = new HashMap<>();

		faultMap.put("10", "Controller over voltage");
		faultMap.put("11", "Phase over current");
		faultMap.put("12", "Current sensor calibration");
		faultMap.put("13", "Current sensor over current");
		faultMap.put("14", "Controller over temperature");
		faultMap.put("15", "Motor Hall sensor fault");
		faultMap.put("16", "Controller under voltage");
		faultMap.put("17", "POST static gating test");
		faultMap.put("18", "Network communication timeout");
		faultMap.put("19", "Instantaneous phase over current");
		faultMap.put("110", "Motor over temperature");
		faultMap.put("111", "Throttle voltage outside range");
		faultMap.put("112", "Instantaneous controller over voltage");
		faultMap.put("113", "Internal error");
		faultMap.put("114", "POST dynamic gating test");
		faultMap.put("115", "Instantaneous under voltage");
		faultMap.put("20", "Parameter CRC invalid");
		faultMap.put("21", "Current scaling out of range");
		faultMap.put("22", "Voltage scaling out of range");
		faultMap.put("23", "Headlight undervoltage");
		faultMap.put("24", "Torque sensor error");
		faultMap.put("25", "CAN bus error");
		faultMap.put("26", "Hall stall");
		faultMap.put("27", "Bootloader error");
		faultMap.put("28", "Parameter2 CRC invalid");
		faultMap.put("29", "Hall vs Sensorless position disagree");
		faultMap.put("210", "Unknown fault 210");
		faultMap.put("211", "Unknown fault 211");
		faultMap.put("212", "Unknown fault 212");
		faultMap.put("213", "Unknown fault 213");
		faultMap.put("214", "Unknown fault 214");
		faultMap.put("215", "Unknown fault 215");

		faults = Collections.unmodifiableMap(faultMap);
	}

	public Faults() { }

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

			int faultValue = (value[3] << 8) + (value[4] & 0xff);

			if(service == 0x1 && operation == 0x3) {
				handleFaultsResponse(readingFaultRegisterNumber, faultValue);

				if(readingFaultRegisterNumber == 1) {
					queryFaultsTwo();
					readingFaultRegisterNumber = 2;
				} else if(readingFaultRegisterNumber == 2) {
					queryFaultsOne();
					readingFaultRegisterNumber = 1;
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		caughtFaults = new ArrayList<>();
		displayFaultList();

		setPendingFaultReadDisplay(1);
		setPendingFaultReadDisplay(2);
		queryFaultsOne();
	}

	@Override
	public void onPause() {
		super.onPause();

		requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LocalBroadcastManager.getInstance(requireContext())
			.unregisterReceiver(messageReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.faults);
		bar.setDisplayHomeAsUpEnabled(true);

		return inflater.inflate(R.layout.faults, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_faults_cancel).setOnClickListener(view1 -> NavHostFragment
			.findNavController(Faults.this)
			.navigate(R.id.action_Faults_to_Settings));
	}

	// ------------------------------------------------

	protected void sendIntent(String event) {
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID).putExtra("event", event)
		);
	}

	protected void queryFaultsOne() {
		new Handler(Looper.getMainLooper()).postDelayed(() -> setPendingFaultReadDisplay(1), 400);
		new Handler(Looper.getMainLooper()).postDelayed(() -> sendIntent("read-faults-1"), 500);
	}

	protected void queryFaultsTwo() {
		new Handler(Looper.getMainLooper()).postDelayed(() -> setPendingFaultReadDisplay(2), 400);
		new Handler(Looper.getMainLooper()).postDelayed(() -> sendIntent("read-faults-2"), 500);
	}

	protected void setPendingFaultReadDisplay(int register) {
		for (int bit = 0; bit <= 15; bit++) {
			try {
				setDisplayValue(register, bit, "-");
			} catch(IllegalStateException ignored) {
				// when view ceases to exist due to navigating away but delayed thread calls setPendingFaultReadDisplay
			}
		}
	}

	protected void handleFaultsResponse(int register, int value) {
		//Log.d("current_reg", String.valueOf(register));
		//Log.d("value", String.valueOf(value));

		for (int bit = 0; bit <= 15; bit++) {
			int bitAtPosition = getBitAtPosition(value, bit);
			setDisplayValue(register, bit, String.valueOf(bitAtPosition));

			if(bitAtPosition == 1) {
				addFault(register, bit);
			}

			displayFaultList();
		}
	}

	protected void setDisplayValue(int register, int bit, String value) {
		String id = String.format("faults%s_bit%s", register, bit);

		TextView textView = requireView().findViewById(
			getResources().getIdentifier(id, "id", getActivity().getPackageName())
		);

		textView.setText(value);
	}

	protected int getBitAtPosition(int value, int position) {
		return (value >> position) & 1;
	}

	protected void addFault(int register, int bit) {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
		String hour = formatter.format(date);

		String faultKey = String.format("%s%s", register, bit);
		try {
			String faultText = faults.get(faultKey);
			caughtFaults.add(String.format("%s %s", hour, faultText));
		} catch(Exception e) {
			Log.e("faults", "Failed to append fault", e);
		}
	}

	protected void displayFaultList() {
		int faultListSize = caughtFaults.size();

		if(faultListSize == 0) {
			((TextView) requireView().findViewById(R.id.faults_list_text)).setText(R.string.faults_placeholder);
			return;
		}

		if(faultListSize > 200) {
			caughtFaults = caughtFaults.subList(faultListSize-200, faultListSize);
		}

		((TextView) requireView().findViewById(R.id.faults_list_text)).setText(Converter.concatList(caughtFaults, "\n"));
	}
}