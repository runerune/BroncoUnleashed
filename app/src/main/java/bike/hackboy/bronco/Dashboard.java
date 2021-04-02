package bike.hackboy.bronco;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Arrays;

import bike.hackboy.bronco.bean.DashboardBean;
import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;

public class Dashboard extends Fragment {
	protected boolean locked = true;
	protected boolean hasEnabledNotifications = false;
	protected View view = null;

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
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
						DashboardBean db = (new DashboardBean()).fromProtobuf(DashboardProto.Dashboard.parseFrom(value));
						updateDashboard(db);
					} catch (InvalidProtocolBufferException e) {
						// ignore, this happens when bike is locked so don't spam it
						//Log.e("ch_value", "could not parse as protobuf", e);
					}
				break;
			}
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();

		assert bar != null;
		bar.setTitle(R.string.dashboard);
		bar.setDisplayHomeAsUpEnabled(false);

		return inflater.inflate(R.layout.dashboard, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LocalBroadcastManager.getInstance(requireContext())
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		sendIntent("read-lock");

		if(!hasEnabledNotifications) {
			sendIntent("enable-notify");
			hasEnabledNotifications = true;
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LocalBroadcastManager.getInstance(requireContext())
			.unregisterReceiver(messageReceiver);
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		this.view = view;

		view.findViewById(R.id.button_goto_settings).setOnClickListener(view1 -> NavHostFragment
			.findNavController(Dashboard.this)
			.navigate(R.id.action_Dashboard_to_Settings));

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

		view.findViewById(R.id.button_disconnect).setOnClickListener(view6 -> {
			sendIntent("disconnect");
		});
	}

	protected void sendIntent(String event) {
		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID).putExtra("event", event)
		);
	}

	@SuppressLint("DefaultLocale")
	protected void updateDashboard(DashboardBean db) {
		try {
			//Log.d("dashboard_state", "on dashboard state");
			//Log.d("dash_parsed", db.toString());

			view.findViewById(R.id.group_settings).setVisibility(View.VISIBLE);

			requireActivity().runOnUiThread(() -> {
				if (locked) {
					view.findViewById(R.id.group_locked).setVisibility(View.VISIBLE);
					view.findViewById(R.id.group_unlocked).setVisibility(View.INVISIBLE);

					return;
				}

				((TextView) view.findViewById(R.id.distance)).setText(db.getDistance());
				((TextView) view.findViewById(R.id.battery_percent)).setText(db.getBattery());
				((TextView) view.findViewById(R.id.duration)).setText(db.getDuration());
				((TextView) view.findViewById(R.id.speed)).setText(db.getSpeed());
				((ProgressBar) view.findViewById(R.id.assistance)).setProgress(db.getRawPower());
				((ProgressBar) view.findViewById(R.id.battery)).setProgress(db.getRawBattery());

				view.findViewById(R.id.group_locked).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.group_unlocked).setVisibility(View.VISIBLE);

				view.findViewById(R.id.button_light_on).setVisibility(db.isLightOn() ? View.INVISIBLE : View.VISIBLE);
				view.findViewById(R.id.icon_light_on).setVisibility(db.isLightOn() ? View.VISIBLE : View.INVISIBLE);

				view.findViewById(R.id.button_light_off).setVisibility(db.isLightOn() ? View.VISIBLE : View.INVISIBLE);
				view.findViewById(R.id.icon_light_off).setVisibility(db.isLightOn() ? View.INVISIBLE : View.VISIBLE);
			});
		} catch (Exception e) {
			Log.e("dashboard_update", "failed in dashboard listener", e);
			e.printStackTrace();
		}
	}

	protected void onLockedChange(boolean locked) {
		this.locked = locked;
		try {
			requireActivity().runOnUiThread(() -> {
				view.findViewById(R.id.group_locked).setVisibility(locked ? View.VISIBLE : View.INVISIBLE);
				view.findViewById(R.id.group_unlocked).setVisibility(locked ? View.INVISIBLE : View.VISIBLE);

				// assume light is off to avoid blinking UI element
				view.findViewById(R.id.icon_light_on).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.button_light_off).setVisibility(View.INVISIBLE);

				// needs to show in sync with the rest of the UI
				view.findViewById(R.id.group_settings).setVisibility(View.VISIBLE);
			});
		} catch (Exception e) {
			Log.e("locked_update", "failed in locked update", e);
			e.printStackTrace();
		}
	}

	protected void vibrate() {
		Vibrator v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
	}
}