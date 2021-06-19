package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import bike.hackboy.bronco.data.Uuid;

public class Faults extends Fragment {
	protected int readingFaultRegisterNumber = 1;
	protected String[] caughtFaults;

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
		return inflater.inflate(R.layout.faults, container, false);
	}

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
			setDisplayValue(register, bit, "-");
		}
	}

	protected void handleFaultsResponse(int register, int value) {
		Log.d("current_reg", String.valueOf(register));
		Log.d("value", String.valueOf(value));

		for (int bit = 0; bit <= 15; bit++) {
			setDisplayValue(register, bit, "0");
		}
	}

	protected void setDisplayValue(int register, int bit, String value) {
		String id = String.format("faults%s_bit%s", register, bit);

		TextView textView = requireView().findViewById(
			getResources().getIdentifier(id, "id", getActivity().getPackageName())
		);

		textView.setText(value);
	}
}