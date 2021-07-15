package bike.hackboy.bronco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import bike.hackboy.bronco.bean.SettingBean;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.view.SettingsAdapter;

public class Settings extends Fragment {
	protected final ArrayList<SettingBean> settings = new ArrayList<>();
	protected RecyclerView recyclerViewSettings;
	protected SettingsAdapter settingsListAdapter;

	protected int aboutTapCount = 0;
	protected boolean developerModeNotified = false;

	protected boolean isUnlocked;
	protected int autoLockTimer = -1;

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			if(!event.equals("on-characteristic-read")) return;

			String uuid = intent.getStringExtra("uuid");
			byte[] value = (intent.getByteArrayExtra("value"));

			switch(uuid.toUpperCase()) {
				case Uuid.characteristicUnlockString:
					Settings.this.isUnlocked = (value[0] == 0x1);
					buildSettings();
				break;

				case Uuid.characteristicSettingsReadString:
					if(value[0] == 0xa && value[1] == 0x3) {
						Settings.this.autoLockTimer = value[4];
						setOverlayVisible(false);
						buildSettings();
					}
				break;
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
		View rootView =  inflater.inflate(R.layout.settings, container, false);
		recyclerViewSettings = rootView.findViewById(R.id.items_list);

		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.settings);
		bar.setDisplayHomeAsUpEnabled(true);

		return rootView;
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		settingsListAdapter = new SettingsAdapter(requireContext(), settings);

		recyclerViewSettings.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewSettings.setAdapter(settingsListAdapter);
		recyclerViewSettings.setItemAnimator(new DefaultItemAnimator());

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(requireContext());

		setOverlayVisible(true);
		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-lock"));
		lbm.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-auto-lock"));
	}

	protected void buildSettings() {
		settings.clear();

		if (isUnlocked) {
			settings.add(new SettingBean()
				.setName((String) getText(R.string.speed_setting))
				.setDescription((String) getText(R.string.description_speed_setting))
				.setHasArrow(true)
				.setOnClickListener(v ->
					NavHostFragment.findNavController(Settings.this)
						.navigate(R.id.action_settings_to_SpeedSetting))
			);

			settings.add(new SettingBean()
				.setName((String) getText(R.string.field_weakening))
				.setDescription((String) getText(R.string.description_field_weakening))
				.setHasArrow(true)
				.setOnClickListener(v ->
					NavHostFragment.findNavController(Settings.this)
						.navigate(R.id.action_settings_to_FieldWeakening))
			);

			settings.add(new SettingBean()
				.setName((String) getText(R.string.standstill_optimization))
				.setDescription((String) getText(R.string.description_standstill_optimization))
				.setHasArrow(true)
				.setOnClickListener(v ->
					NavHostFragment.findNavController(Settings.this)
						.navigate(R.id.action_Settings_to_standstillOptimization))
			);

			/*settings.add(new SettingBean()
				.setName((String) getText(R.string.torque_gain))
				.setDescription((String) getText(R.string.description_torque_gain))
				.setHasArrow(true)
				.setOnClickListener(v ->
					NavHostFragment.findNavController(Settings.this)
						.navigate(R.id.action_Settings_to_torqueGain))
			);*/

			settings.add(new SettingBean()
				.setName((String) getText(R.string.faults))
				.setDescription((String) getText(R.string.description_faults))
				.setHasArrow(true)
				.setOnClickListener(v ->
					NavHostFragment.findNavController(Settings.this)
						.navigate(R.id.action_Settings_to_faults))
			);

			if(aboutTapCount > 7 || ((MainActivity)getActivity()).isDeveloper())  {
				settings.add(new SettingBean()
					.setName((String) getText(R.string.arbitrary_register_read))
					.setDescription((String) getText(R.string.description_arbitrary_register_read))
					.setHasArrow(true)
					.setOnClickListener(v ->
						NavHostFragment.findNavController(Settings.this)
							.navigate(R.id.action_Settings_to_arbitraryRegisterEdit))
				);
			}
		}

		settings.add(new SettingBean()
			.setName((String) getText(R.string.cby_user_details))
			.setDescription((String) getText(R.string.description_bike_details))
			.setHasArrow(true)
			.setOnClickListener(v ->
				NavHostFragment.findNavController(Settings.this)
					.navigate(R.id.action_settings_to_UserData))
		);

		int[] values = {0, 5, 10, 15, 30};
		String[] items = {
			getString(R.string.disabled),
			String.format(getString(R.string.number_minutes), "5"),
			String.format(getString(R.string.number_minutes), "10"),
			String.format(getString(R.string.number_minutes), "15"),
			String.format(getString(R.string.number_minutes), "30"),
		};

		String autoLockTimerValue = (autoLockTimer > 0) ? String.valueOf(autoLockTimer) : getString(R.string.off);

		settings.add(new SettingBean()
			.setName((String) getText(R.string.auto_lock))
			.setDescription((String) getText(R.string.description_auto_lock))
			.setValue(autoLockTimer > -1 ? autoLockTimerValue : "")
			.setOnClickListener(v -> {
				if(autoLockTimer < 0) return;

				new AlertDialog.Builder(requireContext(), R.style.Theme_Bronco_AlertDialog)
					.setTitle(R.string.auto_lock)
					.setItems(items, (dialog, which) -> setAutoLockTimer(values[which]))
					.show();
			})
		);

		settings.add(new SettingBean()
			.setName((String) getText(R.string.disconnect))
			.setDescription((String) getText(R.string.description_disconnect))
			.setOnClickListener(v ->
				LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
					new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "disconnect")
				))
		);

		settings.add(new SettingBean()
			.setName((String) getText(R.string.about))
			.setDescription((String) getText(R.string.description_about))
			.setOnClickListener(v ->
				new AlertDialog.Builder(requireContext(), R.style.Theme_Bronco_AlertDialog)
					.setTitle(R.string.about_title)
					.setMessage(R.string.credits)
					.setNegativeButton(R.string.got_it, null)
					.setPositiveButton(R.string.visit_sub, (dialog, whichButton) -> {
						dialog.dismiss();
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/cowboybikes/"));
						startActivity(browserIntent);
					})
					.show())
		);

		settings.add(new SettingBean()
			.setName((String) getText(R.string.version))
			.setDescription(String.format(
				"%s (%s) - %s",
				BuildConfig.VERSION_NAME,
				BuildConfig.VERSION_CODE,
				BuildConfig.BUILD_TYPE
			))
			.setOnClickListener(v -> {
				aboutTapCount++;

				if(aboutTapCount > 7 && !developerModeNotified) {
					developerModeNotified = true;
					((MainActivity) getActivity()).setIsDeveloper(true);

					Toast.makeText(requireContext(), "You are now a developer \uD83E\uDDD9", Toast.LENGTH_LONG).show();
					buildSettings();
				}
			})
		);

		requireView().findViewById(R.id.unlock_for_more_options).setVisibility(isUnlocked ? View.GONE : View.VISIBLE);
		requireView().findViewById(R.id.items_list).setVisibility(View.VISIBLE);

		settingsListAdapter.notifyDataSetChanged();
	}

	protected void setAutoLockTimer(int time) {
		if(autoLockTimer == time) return;

		autoLockTimer = -1;
		buildSettings();

		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "set-auto-lock")
				.putExtra("value", time)
		);
	}

	protected void setOverlayVisible(boolean visible) {
		requireView().findViewById(R.id.settings_loading).setVisibility(visible ? View.VISIBLE : View.GONE);
	}

}