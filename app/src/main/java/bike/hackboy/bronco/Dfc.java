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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.view.DfcViewAdapter;


public class Dfc extends Fragment {
	protected final List<DfcProto.Dfc> dfcList = new ArrayList<>();
	protected RecyclerView recyclerViewDfc;
	protected DfcViewAdapter dfcViewAdapter;

	private final Handler readyHandler = new Handler(Looper.getMainLooper());

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
		String event = intent.getStringExtra("event");

		if (!event.equals("on-characteristic-read")) return;
		if (!intent.getStringExtra("uuid").equals(Uuid.characteristicDfcRequestString)) return;

		byte[] value = intent.getByteArrayExtra("value");

		try {
			int expectedLength = (int) value[0];
			byte[] payload = Arrays.copyOfRange(value, 1, value.length);

			if(payload.length != expectedLength) throw new Exception("payload has invalid length");

			DfcProto.Dfc entry = DfcProto.Dfc.parseFrom(payload);
			//if(entry.getDfcId() == DfcProto.DfcId.MODEM_START) return;

			//Log.d("debug", entry.toString());

			dfcList.add(0, entry);
		} catch (Exception e) {
			//Log.e("debug", "failed to parse this dfc:");
			//Log.e("debug", Converter.byteArrayToHexString(value));
			e.printStackTrace();
		}

		debounceReady();
		}
	};

	public Dfc() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		dfcList.clear();
		requestDfcs(requireContext(), 0);
	}

	@Override
	public void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dfc, container, false);
		recyclerViewDfc = rootView.findViewById(R.id.dfc_list);

		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.dfc);
		bar.setDisplayHomeAsUpEnabled(true);

		return rootView;
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		dfcViewAdapter = new DfcViewAdapter(requireContext(), dfcList);

		recyclerViewDfc.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewDfc.setAdapter(dfcViewAdapter);
		recyclerViewDfc.setItemAnimator(new DefaultItemAnimator());

		/*view.findViewById(R.id.).setOnClickListener(view1 -> NavHostFragment
			.findNavController(Dfc.this)
			.navigate(R.id.action_dfc_to_Settings));*/
	}


	public static void requestDfcs(Context context, int offset) {
		LocalBroadcastManager.getInstance(context).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "read-dfc")
				.putExtra("offset", 0)
		);
	}

	private final Runnable ready = () -> dfcViewAdapter.notifyDataSetChanged();

	private void debounceReady() {
		readyHandler.removeCallbacks(ready);
		readyHandler.postDelayed(ready, 100);
	}
}