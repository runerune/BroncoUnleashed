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

import java.util.ArrayList;
import java.util.Arrays;

import bike.hackboy.bronco.bean.SettingBean;
import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.view.SettingsAdapter;

public class Settings extends Fragment {
	protected final ArrayList<SettingBean> settings = new ArrayList<>();
	protected RecyclerView recyclerViewSettings;
	protected SettingsAdapter settingsListAdapter;

	protected final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			if(!event.equals("on-characteristic-read")) return;

			String uuid = intent.getStringExtra("uuid");
			byte[] value = (intent.getByteArrayExtra("value"));

			if(uuid.toUpperCase().equals(Uuid.characteristicUnlockString)) {
				buildSettings(value[0] == 0x1);
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

		LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
			new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "read-lock")
		);
	}

	protected void buildSettings(boolean isUnlocked) {
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
		}

		settings.add(new SettingBean()
			.setName((String) getText(R.string.cby_user_details))
			.setDescription((String) getText(R.string.description_bike_details))
			.setHasArrow(true)
			.setOnClickListener(v ->
				NavHostFragment.findNavController(Settings.this)
					.navigate(R.id.action_settings_to_UserData))
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

		requireView().findViewById(R.id.unlock_for_more_options).setVisibility(isUnlocked ? View.GONE : View.VISIBLE);
		requireView().findViewById(R.id.items_list).setVisibility(View.VISIBLE);

		settingsListAdapter.notifyDataSetChanged();
	}

}