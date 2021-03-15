package bike.hackboy.bronco;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import android.widget.Toast;

import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.gatt.Gatt;
import bike.hackboy.bronco.utils.Converter;

public class Dashboard extends Fragment {
	private ObservableLocked locked;
	private ObservableDashboard dashboard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locked = ((MainActivity) requireActivity()).getObservableLocked();
		dashboard = ((MainActivity) requireActivity()).getObservableDashboard();

		setHasOptionsMenu(true);
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

		try {
			BluetoothGatt connection = ((MainActivity) requireActivity()).getConnection();

			Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
			Gatt.requestReadCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);

			Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
			Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicDashboard);
		} catch (Exception e) {
			Toast.makeText(getActivity(), "Failed to request read lock state", Toast.LENGTH_LONG).show();
			Log.e("read_lock", "failed to read lock state", e);
		}
	}

	@SuppressLint("DefaultLocale")
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

		locked.setListener(() -> {
			requireActivity().runOnUiThread(() -> {
				boolean isLocked = locked.isLocked();

				view.findViewById(R.id.button_unlock).setVisibility(isLocked ? View.VISIBLE : View.INVISIBLE);
				view.findViewById(R.id.button_lock).setVisibility(isLocked ? View.INVISIBLE : View.VISIBLE);

				if (isLocked) {
					view.findViewById(R.id.button_light_on).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_light_off).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.button_goto_set_speed).setVisibility(View.INVISIBLE);
					view.findViewById(R.id.gear).setVisibility(View.INVISIBLE);
				}
			});
		});

		dashboard.setListener(() -> {
			try {
				DashboardProto.Dashboard state = dashboard.getState();
				//Log.d("dashboard_state", "on dashboard state");
				//Log.d("dash_parsed", state.toString());

				boolean isLightOn = (state.getLights() == 1);
				String distance = String.format("%s m", state.getDistance());
				String speed = String.format("%s m", state.getSpeed());

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
					try {

						if (locked.isLocked()) {
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
					} catch (Exception e) {
						Log.e("dashboard_state", "failed in ui thread", e);
					}
				});
			} catch (NullPointerException e) {
				Log.e("dashboard_lsnr", "NPE in dashboard listener", e);
			}
		});

		view.findViewById(R.id.button_unlock).setOnClickListener(view2 -> {
			try {
				BluetoothGatt connection = ((MainActivity) requireActivity()).getConnection();

				Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
				Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.UNLOCK);

				vibrate();
			} catch (Exception e) {
				Toast.makeText(getActivity(), "Failed to unlock", Toast.LENGTH_LONG).show();
				Log.e("cmd_unlock", "failed to unlock", e);
			}
		});

		view.findViewById(R.id.button_lock).setOnClickListener(view3 -> {
			try {
				BluetoothGatt connection = ((MainActivity) requireActivity()).getConnection();

				Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
				Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.LOCK);

				vibrate();
			} catch (Exception e) {
				Toast.makeText(getActivity(), "Failed to lock", Toast.LENGTH_LONG).show();
			}
		});

		view.findViewById(R.id.button_light_on).setOnClickListener(view4 -> {
			try {
				BluetoothGatt connection = ((MainActivity) requireActivity()).getConnection();

				byte[] command = Command.withChecksum(Command.LIGHT_ON);

				Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
				Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
			} catch (Exception e) {
				Toast.makeText(getActivity(), "Failed to turn lights on", Toast.LENGTH_LONG).show();
			}
		});

		view.findViewById(R.id.button_light_off).setOnClickListener(view5 -> {
			try {
				BluetoothGatt connection = ((MainActivity) requireActivity()).getConnection();

				byte[] command = Command.withChecksum(Command.LIGHT_OFF);

				Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
				Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
			} catch (Exception e) {
				Toast.makeText(getActivity(), "Failed to turn lights off", Toast.LENGTH_LONG).show();
			}
		});
	}

	protected void vibrate() {
		Vibrator v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
	}
}