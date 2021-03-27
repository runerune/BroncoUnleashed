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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import bike.hackboy.bronco.bean.SettingsEntityBean;

public class Settings extends Fragment {
	final ArrayList<SettingsEntityBean> settings = new ArrayList<>();
	protected RecyclerView recyclerViewSettings;
	protected SettingsAdapter settingsListAdapter;

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

		buildSettings();
	}

	public void buildSettings() {
		settings.clear();

		settings.add(new SettingsEntityBean()
			.setVisibility(SettingsEntityBean.Visibility.ONLY_CONNECTED)
			.setName((String) getText(R.string.speed_setting))
			.setDescription((String) getText(R.string.description_speed_setting))
			.setHasArrow(true)
			.setOnClickListener(v ->
				NavHostFragment.findNavController(Settings.this)
					.navigate(R.id.action_settings_to_SpeedSetting))
		);

		settings.add(new SettingsEntityBean()
			.setVisibility(SettingsEntityBean.Visibility.ONLY_CONNECTED)
			.setName((String) getText(R.string.field_weakening))
			.setDescription((String) getText(R.string.description_field_weakening))
			.setHasArrow(true)
			.setOnClickListener(v ->
				NavHostFragment.findNavController(Settings.this)
					.navigate(R.id.action_settings_to_FieldWeakening))
		);

		settings.add(new SettingsEntityBean()
			.setVisibility(SettingsEntityBean.Visibility.ALWAYS)
			.setName((String) getText(R.string.cby_user_details))
			.setDescription((String) getText(R.string.description_bike_details))
			.setHasArrow(true)
			.setOnClickListener(v ->
				NavHostFragment.findNavController(Settings.this)
					.navigate(R.id.action_settings_to_UserData))
		);

		settings.add(new SettingsEntityBean()
			.setName((String) getText(R.string.disconnect))
			.setDescription((String) getText(R.string.description_disconnect))
			.setVisibility(SettingsEntityBean.Visibility.ONLY_CONNECTED)
			.setOnClickListener(v ->
				LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
					new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "disconnect")
				))
		);

		settings.add(new SettingsEntityBean()
			.setName((String) getText(R.string.about))
			.setDescription((String) getText(R.string.description_about))
			.setVisibility(SettingsEntityBean.Visibility.ALWAYS)
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

		requireView().findViewById(R.id.items_list).setVisibility(View.VISIBLE);
		settingsListAdapter.notifyDataSetChanged();
	}

}