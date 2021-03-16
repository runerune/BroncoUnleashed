package bike.hackboy.bronco;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.text.DecimalFormat;
import java.util.Arrays;

import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.utils.Converter;

public class Dashboard extends Fragment {
	private boolean locked = true;

	// for status text in notification
	private String distance = null;
	private String uptime = null;

	private View view = null;

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			//Log.d("event", event);

			if(!event.equals("on-characteristic-read")) return;

			String uuid = intent.getStringExtra("uuid");
			byte[] value = (intent.getByteArrayExtra("value"));

			switch (uuid.toUpperCase()) {
				case Uuid.characteristicUnlockString:
					//Log.d("uuid_check", "is a lock service uuid");
					onLockedChange(Arrays.equals(value, Command.LOCK));
				break;

				case Uuid.characteristicDashboardString:
					//Log.d("uuid_check", "is a dashboard uuid");
					try {
						parseDashboard(DashboardProto.Dashboard.parseFrom(value));
					} catch (InvalidProtocolBufferException e) {
						// ignore, this happens when bike is locked so don't spam it
						//Log.e("ch_value", "could not parse as protobuf", e);
					}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		setHasOptionsMenu(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(requireContext())
			.unregisterReceiver(messageReceiver);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.disconnect);
		item.setVisible(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.dashboard, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		sendIntent("read-lock");
		sendIntent("enable-notifications");
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		this.view = view;

		view.findViewById(R.id.button_goto_set_speed).setOnClickListener(view1 -> NavHostFragment
			.findNavController(Dashboard.this)
			.navigate(R.id.action_Dashboard_to_SpeedSetting));

		// don't show buttons before callback fires
		view.findViewById(R.id.button_unlock).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.button_lock).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.button_light_off).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.button_light_on).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.button_goto_set_speed).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.gear).setVisibility(View.INVISIBLE);

		view.findViewById(R.id.button_unlock).setOnClickListener(view2 -> {
			sendIntent("unlock");
			vibrate();
		});

		view.findViewById(R.id.button_lock).setOnClickListener(view3 -> {
			sendIntent("lock");
			vibrate();
		});

		view.findViewById(R.id.button_light_on).setOnClickListener(view4 -> {
			sendIntent("lights-on");
			vibrate();
		});

		view.findViewById(R.id.button_light_off).setOnClickListener(view5 -> {
			sendIntent("lights-off");
			vibrate();
		});
	}

	protected void sendIntent(String event) {
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID).putExtra("event", event)
		);
	}

	protected void sendDashboardIntent() {
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "dashboard_notification")
				.putExtra("locked", locked)
				.putExtra("uptime", uptime)
				.putExtra("distance", distance)
		);
	}

	@SuppressLint("DefaultLocale")
	protected void parseDashboard(DashboardProto.Dashboard state) {
		try {
			//Log.d("dashboard_state", "on dashboard state");
			//Log.d("dash_parsed", state.toString());

			boolean isLightOn = (state.getLights() == 1);
			String speed = String.format("%s", state.getSpeed());

			int rawDistance = state.getDistance();
			String distance;

			if (rawDistance < 1000) {
				distance = String.format("%s m", state.getDistance());
			} else {
				DecimalFormat formatter = new DecimalFormat("#,###.00");
				distance = formatter.format(state.getDistance() / 1000);
			}

			String assistance = (state.getAssistance() == 0 || state.getAssistance() == 3) ? "S" : "D";

			int[] uptime = Converter.secondsToTime(state.getDuration());
			String duration;

			if (uptime[0] > 0) {
				duration = String.format(
					"%02d:%02d:%02d",
					uptime[0],
					uptime[1],
					uptime[2]
				);
			} else {
				duration = String.format(
					"%02d:%02d",
					uptime[1],
					uptime[2]
				);
			}

			requireActivity().runOnUiThread(() -> {
				if (locked) {
					view.findViewById(R.id.button_light_on).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_light_off).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_goto_set_speed).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.gear).setVisibility(View.INVISIBLE);
					return;
				}

				((TextView) view.findViewById(R.id.distance)).setText(distance);
				((TextView) view.findViewById(R.id.duration)).setText(duration);
				((TextView) view.findViewById(R.id.speed)).setText(speed);
				((ProgressBar) view.findViewById(R.id.assistance)).setProgress(state.getPower());
				((Button) view.findViewById(R.id.button_goto_set_speed)).setText(assistance);

				view.findViewById(R.id.button_light_on).setVisibility(isLightOn ? View.INVISIBLE : View.VISIBLE);
				view.findViewById(R.id.button_light_off).setVisibility(isLightOn ? View.VISIBLE : View.INVISIBLE);
				view.findViewById(R.id.button_goto_set_speed).setVisibility(View.VISIBLE);
				view.findViewById(R.id.gear).setVisibility(View.VISIBLE);

				this.uptime = duration;
				this.distance = distance;

				sendDashboardIntent();
			});
		} catch (Exception e) {
			Log.e("dashboard_update", "failed in dashboard listener", e);
		}
	}

	protected void onLockedChange(boolean locked) {
		this.locked = locked;
		try {
			requireActivity().runOnUiThread(() -> {
				view.findViewById(R.id.button_unlock).setVisibility(locked ? View.VISIBLE : View.INVISIBLE);
				view.findViewById(R.id.button_lock).setVisibility(locked ? View.INVISIBLE : View.VISIBLE);

				if (locked) {
					view.findViewById(R.id.button_light_on).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_light_off).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_goto_set_speed).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.gear).setVisibility(View.INVISIBLE);
				}

				sendDashboardIntent();
			});
		} catch (Exception e) {
			Log.e("locked_update", "failed in locked update", e);
		}
	}

	protected void vibrate() {
		Vibrator v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
	}
}