package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.Converter;

public class ArbitraryRegisterEdit extends Fragment {

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

			String stringValue = Converter.byteArrayToHexString(value);
			Log.d("value", stringValue);

			if(stringValue.equals("FF01")) {
				setResultValue(stringValue, "(no value)");
				return;
			}

			if(service == 0x1) {
				switch(operation) {
					case 0x3: // read notification
						int rawValue = (value[3] << 8) + (value[4] & 0xff);
						Log.d("rawValue", String.valueOf(rawValue));

						setResultValue(stringValue, String.valueOf(rawValue));
					break;
					case 0x10: // write success notification

					break;
				}
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
		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.arbitrary_register_read);
		bar.setDisplayHomeAsUpEnabled(true);

		return inflater.inflate(R.layout.arbitrary_register_read, container, false);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		EditText foo = view.findViewById(R.id.register);

		view.findViewById(R.id.read).setOnClickListener(v -> {
			LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(requireContext());

			lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "read-register")
				.putExtra("register", Integer.parseInt(foo.getText().toString()))
			);
		});
	}

	protected void setResultValue(String raw, String parsed) {
		View view = requireView();

		((TextView) view.findViewById(R.id.raw_response_display)).setText(raw);
		((TextView) view.findViewById(R.id.parsed_value_display)).setText(parsed);
	}
}
